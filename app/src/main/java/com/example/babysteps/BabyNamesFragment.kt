package com.example.babysteps

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONArray
import java.io.IOException

class BabyNamesFragment : Fragment() {

    private lateinit var namesRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var genderSpinner: Spinner
    private lateinit var searchButton: Button
    private lateinit var namesAdapter: BabyNamesAdapter

    private val client = OkHttpClient()
    private val namesList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_baby_names, container, false)

        namesRecyclerView = view.findViewById(R.id.namesRecyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        errorTextView = view.findViewById(R.id.errorTextView)
        genderSpinner = view.findViewById(R.id.genderSpinner)
        searchButton = view.findViewById(R.id.searchButton)

        // Set up RecyclerView
        namesAdapter = BabyNamesAdapter(namesList)
        namesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        namesRecyclerView.adapter = namesAdapter

        // Set up Spinner
        val genderOptions = arrayOf("boy", "girl", "neutral")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genderOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = spinnerAdapter

        // Set up Search Button
        searchButton.setOnClickListener {
            fetchBabyNames(genderSpinner.selectedItem.toString())
        }

        return view
    }

    private fun fetchBabyNames(gender: String) {
        // Clear previous results
        namesList.clear()
        namesAdapter.notifyDataSetChanged()

        progressBar.visibility = View.VISIBLE
        errorTextView.visibility = View.GONE
        namesRecyclerView.visibility = View.GONE

        val url = "https://api.api-ninjas.com/v1/babynames?gender=$gender"
        val request = Request.Builder()
            .url(url)
            .addHeader("X-Api-Key", "ZphwFpRAdSKlddOQusSUWg==TQeibpLB8K7BAcva")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                requireActivity().runOnUiThread {
                    progressBar.visibility = View.GONE
                    errorTextView.visibility = View.VISIBLE
                    errorTextView.text = "Error fetching names: ${e.message}"
                }
                Log.e("BabyNamesFragment", "API call failed", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        val names = JSONArray(responseBody)
                        for (i in 0 until names.length()) {
                            namesList.add(names.getString(i))
                        }
                        requireActivity().runOnUiThread {
                            progressBar.visibility = View.GONE
                            if (namesList.isNotEmpty()) {
                                namesRecyclerView.visibility = View.VISIBLE
                                namesAdapter.notifyDataSetChanged()
                            } else {
                                errorTextView.visibility = View.VISIBLE
                                errorTextView.text = "No names found."
                            }
                        }
                    }
                } else {
                    requireActivity().runOnUiThread {
                        progressBar.visibility = View.GONE
                        errorTextView.visibility = View.VISIBLE
                        errorTextView.text = "Error: ${response.message}"
                    }
                    Log.e("BabyNamesFragment", "API call unsuccessful: ${response.message}")
                }
            }
        })
    }
}
