package com.example.babysteps

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.example.babysteps.api.NearbyPlacesResponse
import com.example.babysteps.api.PlacesService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NearestHospitalFragment : Fragment(), OnMapReadyCallback {

    private val tag = "NearestHospitalFragment"
    private val locationPermissionRequestCode = 1
    private var isMapReady = false
    private var nearbyHospitalsCall: Call<NearbyPlacesResponse>? = null

    private lateinit var placesService: PlacesService
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_nearest_hospital, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(tag, "onViewCreated: Initializing services and map fragment")
        placesService = PlacesService.create()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        isMapReady = true
        Log.d(tag, "onMapReady: Map is ready")

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                mMap.isMyLocationEnabled = true
                getCurrentLocationAndNearbyHospitals()
            } catch (e: SecurityException) {
                Log.e(tag, "SecurityException: ${e.message}")
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionRequestCode
            )
        }
    }

    private fun getCurrentLocationAndNearbyHospitals() {
        Log.d(tag, "getCurrentLocationAndNearbyHospitals: Fetching current location")
        if (!isAdded) {
            Log.w(tag, "Fragment not attached, skipping location fetch")
            return
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionRequestCode
            )
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (isAdded && location != null) {
                    Log.d(tag, "Current location: ${location.latitude}, ${location.longitude}")
                    updateMapWithLocation(location)
                } else {
                    Log.w(tag, "Last location is null or fragment not attached")
                    requestLocationUpdates()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(tag, "Failed to get location: ${exception.message}")
            }
    }

    private fun requestLocationUpdates() {
        val locationRequest = com.google.android.gms.location.LocationRequest.Builder(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            10000L
        ).setMinUpdateIntervalMillis(5000L).build()

        val locationCallback = object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                if (!isAdded) {
                    Log.w(tag, "Fragment not attached, skipping location update")
                    return
                }
                locationResult.lastLocation?.let { location ->
                    Log.d(tag, "Location update received: ${location.latitude}, ${location.longitude}")
                    updateMapWithLocation(location)
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun updateMapWithLocation(location: Location) {
        if (!isAdded) {
            Log.w(tag, "Fragment not attached, skipping map update")
            return
        }

        Log.d(tag, "Updating map with location: ${location.latitude}, ${location.longitude}")
        val currentLatLng = LatLng(location.latitude, location.longitude)

        mMap.addMarker(
            MarkerOptions()
                .position(currentLatLng)
                .title("My Location")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_location))
        )
        mMap.addCircle(
            CircleOptions()
                .center(currentLatLng)
                .radius(1250.0)
                .strokeWidth(2f)
                .strokeColor(ContextCompat.getColor(requireContext(), R.color.BabySteps_primary))
                .fillColor(ContextCompat.getColor(requireContext(), R.color.BabySteps_secondary_low_op))
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, DEFAULT_ZOOM))

        if (isMapReady && viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            getNearbyHospitals(location)
        }
    }

    private fun getNearbyHospitals(location: Location) {
        val apiKey = getString(R.string.google_maps_key)
        Log.d(tag, "Fetching nearby hospitals with API key: $apiKey")
        nearbyHospitalsCall?.cancel()
        nearbyHospitalsCall = placesService.nearbyPlaces(
            apiKey = apiKey,
            location = "${location.latitude},${location.longitude}",
            radiusInMeters = 2000,
            placeType = "hospital"
        )
        nearbyHospitalsCall?.enqueue(object : Callback<NearbyPlacesResponse> {
            override fun onResponse(call: Call<NearbyPlacesResponse>, response: Response<NearbyPlacesResponse>) {
                if (response.isSuccessful) {
                    val places = response.body()?.results ?: emptyList()
                    Log.d(tag, "Found ${places.size} nearby hospitals")
                    addHospitalMarkers(places)
                } else {
                    Log.e(tag, "Failed to get nearby hospitals. Response code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<NearbyPlacesResponse>, t: Throwable) {
                Log.e(tag, "Failed to get nearby hospitals: ${t.message}")
            }
        })
    }

    private fun addHospitalMarkers(places: List<com.example.babysteps.model.Place>) {
        if (!isAdded) {
            Log.w(tag, "Fragment not attached, skipping marker addition")
            return
        }

        Log.d(tag, "Adding markers for ${places.size} hospitals")
        for (place in places) {
            mMap.addMarker(
                MarkerOptions()
                    .position(place.geometry.location.latLng)
                    .title(place.name)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_hospital_location))
            )
        }
    }

    companion object {
        private const val DEFAULT_ZOOM = 13f
    }
}
