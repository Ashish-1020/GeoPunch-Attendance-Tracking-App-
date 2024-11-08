package com.example.geopunch.admin

import android.app.Fragment
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.geopunch.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AddEmployeeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddEmployeeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var desig : AutoCompleteTextView
    private lateinit var offic : AutoCompleteTextView
    private lateinit var depart : AutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_employee, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val addemp = view.findViewById<Button>(R.id.add_emp)
        desig = view.findViewById(R.id.des)
        offic = view.findViewById(R.id.off)
        depart = view.findViewById(R.id.dep)

        addemp.setOnClickListener{
            Toast.makeText(context, "New employee added", Toast.LENGTH_SHORT).show()
        }

        val itemsd = arrayOf("Manager", "Senior Engineer", "Junior Engineer", "Assistant Manager", "Executive Director", "Chief Manager", "Deputy General Manager", "General Manager", "Officer", "Superintendent")
        val adapterd = ArrayAdapter(requireContext(), R.layout.dropdown_menu_popup_item, itemsd)
        desig.setAdapter(adapterd)

        desig.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position) as String
            desig.setText(selectedItem)
            desig.dismissDropDown()
        }

        val itemso = arrayOf("Head Office New Delhi", "Zonal Office Mumbai", "Zonal Office Kolkata", "Zonal Office Hyderabad", "Zonal Office Bengaluru", "Zonal Office Noida", "Regional Office Jaipur", "Regional Office Ahmedabad", "Regional Office Chennai")

        val adaptero = ArrayAdapter(requireContext(), R.layout.dropdown_menu_popup_item, itemso)
        offic.setAdapter(adaptero)

        offic.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position) as String
            offic.setText(selectedItem)
            offic.dismissDropDown()
        }

        val itemsdp = arrayOf("Human Resources", "Finance", "Engineering", "Marketing", "Operations", "Legal", "Procurement", "Sales", "Research and Development", "Information Technology")

        val adapterdp = ArrayAdapter(requireContext(), R.layout.dropdown_menu_popup_item, itemsdp)
        depart.setAdapter(adapterdp)

        depart.setOnItemClickListener { parent, view, position, id ->
            val selectedItem = parent.getItemAtPosition(position) as String
            depart.setText(selectedItem)
            depart.dismissDropDown()
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AddEmployee.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddEmployeeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}