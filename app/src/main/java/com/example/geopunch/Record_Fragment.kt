package com.example.geopunch

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


/**
 * A simple [Fragment] subclass.
 * Use the [Record_Fragment.newInstance] factory method to
 * create an instance of this fragment.
 */class Record_Fragment : Fragment() {
    // List to store EntryItem objects
    private val items = mutableListOf<EntryItem>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var cardAnim:CardView
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var NodataAnim: LottieAnimationView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var selectedDateForFurtherUse: String? = null
    private var workingHourdis:TextView?=null
    var totalWorkingHours: Long = 0L // Initialize total working hours as 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_record_, container, false)

        val textViewDate: TextView = view.findViewById(R.id.date)
        val selectdate:TextView =view.findViewById(R.id.textView_date)


        // Set today's date in the desired format by default
        val todayDate = getCurrentFormattedDate()
        textViewDate.text = todayDate

        selectdate.setOnClickListener {
            val calendar = Calendar.getInstance()
            // Retrieve previously selected date or use the current date as default
            val year = selectedDateForFurtherUse?.let {
                SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).parse(it)?.year?.plus(1900) ?: calendar.get(Calendar.YEAR)
            } ?: calendar.get(Calendar.YEAR)
            val month = selectedDateForFurtherUse?.let {
                SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).parse(it)?.month ?: calendar.get(Calendar.MONTH)
            } ?: calendar.get(Calendar.MONTH)
            val day = selectedDateForFurtherUse?.let {
                SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).parse(it)?.date ?: calendar.get(Calendar.DAY_OF_MONTH)
            } ?: calendar.get(Calendar.DAY_OF_MONTH)

            // Open DatePickerDialog
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Get month name and ordinal suffix for the day
                    val monthName = getMonthName(selectedMonth)
                    val dayWithSuffix = getDayWithSuffix(selectedDay)
                    // Format the selected date as "15 September 2024"
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                    val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
                    val formattedDate = dateFormat.format(selectedCalendar.time)
                    selectedDateForFurtherUse = formattedDate

                    // Format the date as "23rd Jun, 2024"
                    val selectedDate = "$dayWithSuffix $monthName, $selectedYear"
                    textViewDate.text = selectedDate
                    fetchEntryDetails()
                },
                year, month, day
            )


            datePickerDialog.datePicker.maxDate = calendar.timeInMillis
            // Show the date picker dialog
            datePickerDialog.show()
        }
        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

 cardAnim=view.findViewById(R.id.AnimViewCard)

        NodataAnim=view.findViewById(R.id.nodataAnim)

        workingHourdis=view.findViewById(R.id.workingHours)
        // Initialize the adapter with the list of items
        itemAdapter = ItemAdapter(items)
        recyclerView.adapter = itemAdapter

        // Fetch the entry details and populate the RecyclerView
        fetchEntryDetails()
        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            totalWorkingHours=0L
            fetchEntryDetails() // Refresh the data
        }

        return view
    }

    data class EntryItem(
        val checkInTime: String,
        val checkOutTime: String,
        val typeEntry: String,
        val typeExit: String,
        val late: String?,
        val office: String
    )

    class ItemAdapter(private val items: List<EntryItem>) :
        RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

        // Define a ViewHolder class
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val checkIn: TextView = itemView.findViewById(R.id.timeline_title)
            val checkOut: TextView = itemView.findViewById(R.id.timeline_title2)
            val typeEntry: TextView = itemView.findViewById(R.id.timeline_description)
            val typeExit: TextView = itemView.findViewById(R.id.timeline_description2)
            val late: TextView = itemView.findViewById(R.id.lateTime)
            val office: TextView = itemView.findViewById(R.id.locationName)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_auto, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.checkIn.text = item.checkInTime
            holder.checkOut.text = item.checkOutTime
            holder.typeEntry.text = item.typeEntry
            holder.typeExit.text = item.typeExit
            holder.late.text = item.late
            holder.office.text = item.office
        }

        override fun getItemCount(): Int = items.size
    }

    fun fetchEntryDetails() {


        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.email ?: return // Ensure user is authenticated

        var formattedWorkingHours:String
        val db = FirebaseFirestore.getInstance()

        // Clear the items list before fetching new data
        items.clear()

        // Get today's date in the format "04 September 2024"
        val today = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(System.currentTimeMillis())

        // Fetch specific documents using today's date
        db.collection("Employees Data")
            .document(userId) // Use email as the user ID
            .collection("Entries Data")
            .get() // Fetch all documents in "Entries Data"
            .addOnSuccessListener { documents ->
                for (document in documents) {

                    val documentDate = document.getString("date") ?: "N/A"

                    if(selectedDateForFurtherUse==null){
                        selectedDateForFurtherUse=today
                    }
//                    Toast.makeText(requireContext(), "Showing the data of $selectedDateForFurtherUse", Toast.LENGTH_SHORT).show()
                    // Check if the document's date matches today's date
                    if (documentDate == selectedDateForFurtherUse) {




                        val timestamp = document.getTimestamp("entryTime")
                        val checkInTime = timestamp?.toDate()?.let { date ->
                            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault()) // Format: 7:30 PM
                            sdf.format(date)
                        } ?: "N/A"

                        val timestamp1 = document.getTimestamp("exitTime")
                        val checkOutTime = timestamp1?.toDate()?.let { date ->
                            val sdf = SimpleDateFormat("h:mm a", Locale.getDefault()) // Format: 7:30 PM
                            sdf.format(date)
                        } ?: "N/A"

                        var typeEntry = "Automatic Entry"
                        var typeExit = "Automatic Exit"
                        val late = "Late: N/A"
                        val office = document.getString("officeLocation") ?: "N/A"

                        if(office=="Substation-1"){
                            typeEntry = "Manual Entry"
                             typeExit = "Manual Exit"
                        }
                         if (timestamp != null && timestamp1 != null) {
                            val entryTimeInMillis = timestamp.toDate().time
                            val exitTimeInMillis = timestamp1.toDate().time
                            val diffInMillis = exitTimeInMillis - entryTimeInMillis



                            // Append to total working hours
                            totalWorkingHours += diffInMillis
                            // Calculate hours and minutes

                            // You can store or display formattedWorkingHours as needed
                        }
                        // Create an EntryItem object and add it to the list
                        val entryItem = EntryItem(
                            checkInTime,
                            checkOutTime,
                            typeEntry,
                            typeExit,
                            late,
                            office
                        )
                        items.add(entryItem) // Add the new item to the list
                    }
                }




                val hours = (totalWorkingHours / (1000 * 60 * 60)) % 24
                val minutes = (totalWorkingHours / (1000 * 60)) % 60

                formattedWorkingHours = "$hours:$minutes hrs"

                workingHourdis!!.text=formattedWorkingHours
//                Toast.makeText(requireContext(), "working hour: $formattedWorkingHours", Toast.LENGTH_LONG).show()


                // Notify the adapter that the data has changed
                itemAdapter.notifyDataSetChanged()


                // Check if the items list is empty
                if (items.isEmpty()) {
                    recyclerView.visibility = View.GONE
                    swipeRefreshLayout.visibility=View.GONE// Hide the card
                    cardAnim.visibility=View.VISIBLE
                    NodataAnim.visibility = View.VISIBLE // Show the Lottie animation
                    NodataAnim.playAnimation()
                workingHourdis!!.text="0:00 hrs"// Play the animation
                } else {
                    cardAnim.visibility=View.GONE
                    swipeRefreshLayout.visibility=View.VISIBLE
                    recyclerView.visibility = View.VISIBLE // Show the card
                   NodataAnim.visibility = View.GONE // Hide the animation
                   NodataAnim.pauseAnimation() // Stop the animation
                }
                swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error fetching entry: ${e.message}", Toast.LENGTH_LONG).show()
            }


    }


    // Function to get month name from month number
    private fun getMonthName(month: Int): String {
        val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        return months[month]
    }

    // Function to add ordinal suffix (st, nd, rd, th) to the day
    private fun getDayWithSuffix(day: Int): String {
        return when (day) {
            1, 21, 31 -> "$day" + "st"
            2, 22 -> "$day" + "nd"
            3, 23 -> "$day" + "rd"
            else -> "$day" + "th"
        }
    }
    // Function to get today's date in the desired format
    private fun getCurrentFormattedDate(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // Get month name and ordinal suffix for the day
        val monthName = getMonthName(month)
        val dayWithSuffix = getDayWithSuffix(day)

        // Return the formatted date as "23rd Jun, 2024"
        return "$dayWithSuffix $monthName, $year"
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Record_Fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
