package com.log.compass.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.log.compass.database.Destination
import com.log.compass.databinding.FragmentSetDestinationBinding
import com.log.compass.viewmodel.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SetDestination : Fragment() {

    private var _binding: FragmentSetDestinationBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ViewModel
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSetDestinationBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)
        viewModel = ViewModelProvider(this).get(ViewModel::class.java)

        binding.setButton.setOnClickListener {
            if (validateInputs(binding.latitudeInput.text.toString(), binding.longitudeInput.text.toString())){
                val inputs = getStringInputs()
                CoroutineScope(Dispatchers.Main).launch {
                    // save destination to database
                    val destination = Destination(inputs[0], inputs[1])
                    viewModel.setDestination(destination)

                    // hide keyboard
                    val keyView = activity?.currentFocus
                    keyView?.let { v ->
                        val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                        inputMethodManager?.hideSoftInputFromWindow(v.windowToken, 0)
                    }
                    // turn back to main fragment
                    navController.popBackStack()
                }
            }
        }
    }


    // check if input areas filled correctly
    private fun validateInputs(latitude: String, longitude: String): Boolean {

        when {
            latitude.isEmpty() -> {
                binding.latitudeInput.error = "Latitude cannot be empty !"
                return false
            }
            latitude.length > 10 && latitude.toDouble() > 0 -> {
                binding.latitudeInput.error = "Not more than 8 digits !"
                return false
            }
            latitude.length > 11 && latitude.toDouble() < 0 -> {
                binding.latitudeInput.error = "Not more than 8 digits !"
                return false
            }
            latitude.toDouble() > 90 -> {
                binding.latitudeInput.error = "Latitude can not be higher than 90 !"
                return false
            }
            latitude.toDouble() < -90 -> {
                binding.latitudeInput.error = "Latitude can not be lower than -90 !"
                return false
            }
            longitude.isEmpty() -> {
                binding.longitudeInput.error = "Longitude cannot be empty !"
                return false
            }
            longitude.length > 11 && longitude.toDouble() > 0 -> {
                binding.longitudeInput.error = "Not more than 9 digits !"
                return false
            }
            longitude.length > 12 && longitude.toDouble() < 0-> {
                binding.longitudeInput.error = "Not more than 9 digits !"
                return false
            }
            longitude.toDouble() > 180 -> {
                binding.longitudeInput.error = "Longitude can not be higher than 180 !"
                return false
            }
            longitude.toDouble() < -180 -> {
                binding.longitudeInput.error = "Longitude can not be lower than -180 !"
                return false
            }

            else -> return true
        }

    }

    // return inputs as String
    private fun getStringInputs(): Array<String> = arrayOf(binding.latitudeInput.text.toString().toDouble().toString(), binding.longitudeInput.text.toString().toDouble().toString())


}