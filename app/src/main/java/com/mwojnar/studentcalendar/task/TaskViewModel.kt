package com.mwojnar.studentcalendar.task

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mwojnar.studentcalendar.R
import com.mwojnar.studentcalendar.database.Course
import com.mwojnar.studentcalendar.database.CoursesTableDao
import com.mwojnar.studentcalendar.database.Task
import com.mwojnar.studentcalendar.database.TasksTableDao
import com.mwojnar.studentcalendar.helpers.InputViewModel
import com.mwojnar.studentcalendar.helpers.ValueDropdownItem
import com.mwojnar.studentcalendar.helpers.cancelNotification
import com.mwojnar.studentcalendar.helpers.scheduleNotification
import java.util.*
import java.util.concurrent.TimeUnit


class TaskViewModel(
    private val activity: Activity,
    dao: TasksTableDao,
    private val coursesDao: CoursesTableDao,
    taskId: Long?
) : InputViewModel<Task>(activity, dao, taskId, Task()) {
    val remindersArray = arrayOf(
        ValueDropdownItem(getString(R.string.none), -1),
        ValueDropdownItem(getString(R.string.at_the_time), 0),
        ValueDropdownItem(getString(R.string.five_min), TimeUnit.MINUTES.toMillis(5)),
        ValueDropdownItem(getString(R.string.ten_min), TimeUnit.MINUTES.toMillis(10)),
        ValueDropdownItem(getString(R.string.fifteen_min), TimeUnit.MINUTES.toMillis(15)),
        ValueDropdownItem(getString(R.string.thirty_min), TimeUnit.MINUTES.toMillis(30)),
        ValueDropdownItem(getString(R.string.one_h), TimeUnit.HOURS.toMillis(1)),
        ValueDropdownItem(getString(R.string.two_h), TimeUnit.HOURS.toMillis(2)),
        ValueDropdownItem(getString(R.string.twelve_h), TimeUnit.HOURS.toMillis(12)),
        ValueDropdownItem(getString(R.string.one_day), TimeUnit.DAYS.toMillis(1)),
        ValueDropdownItem(getString(R.string.two_days), TimeUnit.DAYS.toMillis(2)),
        ValueDropdownItem(getString(R.string.one_week), TimeUnit.DAYS.toMillis(7))
    )

    val remindersTextMap = mapOf(
        Pair(-1, getString(R.string.none)),
        Pair(0, getString(R.string.at_the_time)),
        Pair(TimeUnit.MINUTES.toMillis(5), getString(R.string.five_min)),
        Pair(TimeUnit.MINUTES.toMillis(10), getString(R.string.ten_min)),
        Pair(TimeUnit.MINUTES.toMillis(15), getString(R.string.fifteen_min)),
        Pair(TimeUnit.MINUTES.toMillis(30), getString(R.string.thirty_min)),
        Pair(TimeUnit.HOURS.toMillis(1), getString(R.string.one_h)),
        Pair(TimeUnit.HOURS.toMillis(2), getString(R.string.two_h)),
        Pair(TimeUnit.HOURS.toMillis(12), getString(R.string.twelve_h)),
        Pair(TimeUnit.DAYS.toMillis(1), getString(R.string.one_day)),
        Pair(TimeUnit.DAYS.toMillis(2), getString(R.string.two_days)),
        Pair(TimeUnit.DAYS.toMillis(7), getString(R.string.one_week))
    )

    val priorityArray = arrayOf(
        ValueDropdownItem(getString(R.string.high), 2),
        ValueDropdownItem(getString(R.string.normal), 1),
        ValueDropdownItem(getString(R.string.low), 0)
    )

    val priorityTextMap = mapOf(
        Pair(2, getString(R.string.high)),
        Pair(1, getString(R.string.normal)),
        Pair(0, getString(R.string.low))
    )

    private val coursesMutableLiveData = MutableLiveData<List<Course>>()
    val coursesList: LiveData<List<Course>>
        get() = coursesMutableLiveData

    private val selectedCourseMutableLiveData = MutableLiveData<Course>()
    val selectedCourse: LiveData<Course>
        get() = selectedCourseMutableLiveData

    init {
        dbOperation { coursesMutableLiveData.postValue(coursesDao.getAll()) }
    }

    fun saveData() {
        super.saveData { taskId ->
            // Schedule notification
            model.value?.reminder?.let {
                val notificationTime = model.value?.whenDateTime?.time?.minus(it)

                if (it >= 0) {
                    scheduleNotification(
                        activity.applicationContext, taskId, model.value?.title!!, notificationTime!!
                    )
                } else {
                    cancelNotification(
                        activity.applicationContext, taskId
                    )
                }
            }
        }
    }

    override fun deleteData() {
        super.deleteData()

        // Cancel notification if it was scheduled
        if (model.value?.reminder != null) {
            cancelNotification(activity.applicationContext, model.value?.taskId!!)
        }
    }

    fun loadCourseName() {
        if (model.value?.courseId != null) {
            dbOperation { selectedCourseMutableLiveData.postValue(coursesDao.get(model.value?.courseId!!)) }
        }
    }

    fun setCourse(courseId: Long) {
        modelMutableLiveData.value?.courseId = courseId
    }

    fun setPriority(priority: Int) {
        modelMutableLiveData.value?.priority = priority
    }

    fun setReminder(reminderTime: Long) {
        modelMutableLiveData.value?.reminder = reminderTime
    }

    fun setWhenDateTime(whenDateTime: Date) {
        modelMutableLiveData.value?.whenDateTime = whenDateTime
    }
}