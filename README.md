# AlarmBuddy

created by Jakob Huber - cc231025@fhstp.ac.at - as a final Project for the Mobile Coding Course
FH St.Pölten - BCC WS24

### About

AlarmBuddy is intended to be as annoying as possible to even get the heaviest sleepers out of Bed and keep them from falling back asleep.
This is done with 4 different Tasks that can be set
 - Barcode Scanner:
    Set a barcode far away from your bed, if you activate this feature the App will require you to scan this barcode before it stops the alarm.
 - Shaker
    Shake the Phone a certain number of times to stop the alarm
 - Math Problems
    Solve 5 Math Problems to continue
 - Memory
    Play a game of colorful Memory to stop the Alarm

Additionally the Alarms will also trigger if the Phone is in standby and/or the App is closed. Also the preset Alarmvolume cannot be turned down and is locked until the alarm is stopped.
To Achieve this multiple permissions were required - mainly "Allow to edit Systems settings" allowed me to achieve this

To Store Different Alarms and Barcodes a Dao Database was integrate. 
Alarms were managed via the AlarmManager and are "exact alarms". 
After setting them the AlarmManager will run in background and once triggered be caught by an AlarmReceiver which handles the following things
- Starting the Activity with a special Intent
- Set SharedPreferences to make sure the App will start correctly even it was terminated multiple times
- Manage the Media Player to play the alarm sounds
- Send a Notification to the user
- Handle Snooze function to disable the alarm for 10 seconds

When The App is opened with the specified Intent the "Ringing" composable will be called which manages all activated tasks and allows the user to disable the alarm. 
 