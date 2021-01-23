package com.log.compass.fragments


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.log.compass.Compass
import com.log.compass.DestinationCalculator
import com.log.compass.LocationManager
import com.log.compass.R
import com.log.compass.databinding.FragmentCompassBinding
import com.log.compass.viewmodel.ViewModel


class CompassFragment : Fragment() {

    private var _binding: FragmentCompassBinding? = null
    private val binding get() = _binding!!


    private lateinit var navController: NavController
    private lateinit var viewModel: ViewModel

    private lateinit var compass: Compass
    private lateinit var locationManager: LocationManager
    private lateinit var destinationCalculator: DestinationCalculator


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentCompassBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this).get(ViewModel::class.java)
        activity?.let {
            compass = Compass(it, viewModel)
            val lifecycleOwner: LifecycleOwner = this
            locationManager = LocationManager(it, viewModel)
            destinationCalculator = DestinationCalculator(viewModel, lifecycleOwner)
        }
        binding.model = viewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        checkPermissions()
        binding.setDestination.setOnClickListener {
            if (locationManager.permission) {
                navController.navigate(R.id.action_compassFragment_to_setDestination)
            } else {
                locationManager.checkPermissions()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        compass.registerListeners()
    }

    override fun onPause() {
        super.onPause()
        compass.unregisterListeners()
    }

    private fun checkPermissions(): Boolean {
        if (activity != null) {
            val fineLocation = ActivityCompat.checkSelfPermission(
                requireActivity().applicationContext, Manifest.permission.ACCESS_FINE_LOCATION
            )
            if (fineLocation != PackageManager.PERMISSION_GRANTED) {
                if(android.os.Build.VERSION.SDK_INT >= 23) {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 42)
                } else {
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 42)
                }
                return false
            } else {
                return true
            }
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            42 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationManager.permission = true
                    locationManager.startServiceIntent()
                } else {
                    Toast.makeText(activity, "Permissions denied.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}