package com.example.babysteps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment

class PdfViewerFragment : Fragment() {

    companion object {
        private const val PDF_URL_KEY = "pdf_url"

        fun newInstance(pdfUrl: String): PdfViewerFragment {
            val fragment = PdfViewerFragment()
            val args = Bundle()
            args.putString(PDF_URL_KEY, pdfUrl)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val webView = WebView(requireContext())
        val pdfUrl = arguments?.getString(PDF_URL_KEY)

        webView.settings.apply {
            javaScriptEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        webView.webViewClient = WebViewClient()
        pdfUrl?.let {
            // Remove the "alt=media" query parameter
            val cleanedUrl = it.replace("alt=media", "")
            val googleDocsUrl = "https://docs.google.com/gview?embedded=true&url=$cleanedUrl"
            webView.loadUrl(googleDocsUrl)
        }

        return webView
    }

}
