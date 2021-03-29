# Gusto Work Time Tracker

## Disclaimer :)

Being a contractor at the end of every other working week I have to log my working time into system called Gusto (https://gusto.com). Gusto has not very userfriendly interface. It takes me from 30 minutes to 1 hour to add data into that system. Additionally I have to send info table with the same data to my manager via email. I am not a fan of work duplication as well as repeat same dummy actions every other week. So I decide to use an Excel file as a source of truth. Every new spreadsheet will define biweely working period in some format. Excel file will be parsed, records collected and data populated into Gusto time tracking system.

## Specificy of implementation
All data is loged into [BiWeekly Status](#GustoWorkTimeTracker/blob/master/src/main/resources/BiWeeklyStatus.xlsx)

For example:
| Date | Start Work | End Work | Start Break | Break Duration| Description
| :---:  | :---:  | :---:  | :---: | :---: | :---: |
| 3/15/21 | 9:00:00 AM | 6:30:00 PM | 12:30:00 PM | 30 | qTest - TESLA Integration
| 3/16/21 | 9:00:00 AM | 6:00:00 PM | 12:00:00 PM | 45 | qTest - TESLA Integration

**secrets.properties** file holds _user_email_ & _user_password_ props that will be used to login into Gusto. File should be created under [src/main/resources](#GustoWorkTimeTracker/blob/master/src/main/resources/)

Run GustoTracker.java and less than a minute you will have you something similar to this:

<img width="1472" alt="Screen Shot 2021-03-28 at 7 05 37 PM" src="https://user-images.githubusercontent.com/76735/112778189-cd107000-8ff8-11eb-9a86-5a8d35c94408.png">

I have used my lovely [Selenium](https://www.selenium.dev/) which I have never worked with for commercial projects, but that supports so much when you need to mimic user action on UIs.
