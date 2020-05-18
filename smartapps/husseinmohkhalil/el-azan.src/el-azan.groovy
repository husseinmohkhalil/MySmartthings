/**
 *  Azan
 *
 *  Copyright 2020 Hussein Khalil
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed 
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
 name: "El-Azan",
 namespace: "husseinmohkhalil",
 author: "Hussein Khalil",
 description: "this smart app trigger google home speaker or group to call for Azan ",
 category: "My Apps",
 iconUrl: "https://i.pinimg.com/originals/eb/0f/40/eb0f40923cdaf3abaaf473ca1f15a9ee.png",
 iconX2Url: "https://i.pinimg.com/originals/eb/0f/40/eb0f40923cdaf3abaaf473ca1f15a9ee.png",
 iconX3Url: "https://i.pinimg.com/originals/eb/0f/40/eb0f40923cdaf3abaaf473ca1f15a9ee.png")


preferences {



 section("Fajr") {
  input(name: "FajrTargets", type: "capability.musicPlayer", title: "Target Google Home Speakers or Groups", multiple: true, required: true)
  input(name: "FajrVolume", type: "number", title: "Target Volume", required: true, defaultValue: "50")
  input(name: "FajrIsActive", type: "bool", title: "Is Active", required: false, defaultValue: true)

 }

 section("Zoher") {
  input(name: "ZoherTargets", type: "capability.musicPlayer", title: "Target Google Home Speakers or Groups", multiple: true, required: true)
  input(name: "ZoherVolume", type: "number", title: "Target Volume", required: true, defaultValue: "50")
  input(name: "ZoherIsActive", type: "bool", title: "Is Active", required: false, defaultValue: true)
 }

 section("Asr") {
  input(name: "AsrTargets", type: "capability.musicPlayer", title: "Target Google Home Speakers or Groups", multiple: true, required: true)
  input(name: "AsrVolume", type: "number", title: "Target Volume", required: true, defaultValue: "50")
  input(name: "AsrIsActive", type: "bool", title: "Is Active", required: false, defaultValue: true)
 }

 section("Maghreb") {
  input(name: "MaghrebTargets", type: "capability.musicPlayer", title: "Target Google Home Speakers or Groups", multiple: true, required: true)
  input(name: "MaghrebVolume", type: "number", title: "Target Volume", required: true, defaultValue: "50")
  input(name: "MaghrebIsActive", type: "bool", title: "Is Active", required: false, defaultValue: true)
 }

 section("Isa") {
  input(name: "IsaTargets", type: "capability.musicPlayer", title: "Target Google Home Speakers or Groups", multiple: true, required: true)
  input(name: "IsaVolume", type: "number", title: "Target Volume", required: true, defaultValue: "50")
  input(name: "IsaIsActive", type: "bool", title: "Is Active", required: false, defaultValue: true)
 }
 
  section("Ramadan") {
   input(name: "IsRamadanModeActive", type: "bool", title: "Is Ramadan", required: true, defaultValue: false)
   input(name: "EnableKidsMode", type: "bool", title: "Enable Kids Mode", required: true, defaultValue: false)
   input(name: "KidsIftarHour", type: "number", title: "Kids Iftar Hour", required: false, defaultValue: "")
   input(name: "KidsIftarTargets", type: "capability.musicPlayer", title: "Target Google Home Speakers or Groups", multiple: true, required: true)
   input(name: "KidsIftarVolume", type: "number", title: "Target Volume", required: true, defaultValue: "50")
}

}

def installed() {
 log.debug "Installed with settings: ${settings}"
 initialize()
}

def updated() {
 log.debug "Updated with settings: ${settings}"
 unsubscribe()
 initialize()
}

def initialize() {

 log.info " The Azan Smartapp Started"
 GoAzan()

}
def GoAzan() {
 try {
  def nextPrayEvent = GetNextPrayEvent();

  def nextAzanTime = nextPrayEvent.Time
  log.info " Azan ${nextPrayEvent.Name} is after ${nextAzanTime} s "

  runIn(nextAzanTime, "PlayAzan", [data: [nextPrayEvent: nextPrayEvent, TargetDevice: nextPrayEvent.TargetDevices]])

  def nextCalculate = nextAzanTime;
  runIn(nextCalculate, GoAzan);
 } catch (e) {
  log.error "something went wrong in Azan Function: $e"
  sendPush("Exception in GoAzan Function")
  sendPush("$e")


  //rerun after 10 min
  runIn(600, GoAzan);
 }
}

def PlayAzan(data) {
try {
  

 def prayEvent = data.nextPrayEvent
 def targetDevices = GetTargetDeviceByName(prayEvent.Name)
 def isActive = prayEvent.IsActive

 if (isActive) {
  sendPush("it is ${prayEvent.Name} Azan Time :) ")
  targetDevices.setLevel(prayEvent.Volume)

  def PlayBackUrl = GetPlayBackUrlBySalahName(prayEvent.Name)
  targetDevices.setTrack(PlayBackUrl);
 }
 
  } catch (e) {
  log.error "something went wrong in playing Azan Function: $e"
  sendPush("Exception in PlayAzan Function")
  sendPush("$e")

  //rerun after 10 min
  runIn(600, GoAzan);
 }
}

def GetNextPrayEvent() {

 def todatDay = new Date().format('dd') as int
 def todayDayIndex = todatDay -1 as int  // because the index in the array [in the api] starts with 0  so day 27 for example is in the 26th index 
 def todatMonth = new Date().format('MM') as int
 def todatYear = new Date().format('yyyy') as int
 def nextAzanTimeInSeconds

 def params = [
  uri: "http://api.aladhan.com/v1/calendar?latitude=47.9568123&longitude=7.7496747&method=2&month=${todatMonth}&year=${todatYear}",
  path: ""
 ]

 try {
  httpGet(params) {
   resp ->

    def outputJasonData = new groovy.json.JsonOutput().toJson(resp.data)
   def JsonObject = new groovy.json.JsonSlurper().parseText(outputJasonData)

   assert JsonObject instanceof Map
   assert JsonObject.data instanceof List
   assert JsonObject.data[todayDayIndex] instanceof Map
   assert JsonObject.data[todayDayIndex].timings instanceof Map

   //get timings [hh][mm] for each pray     
   def FajrTime = JsonObject.data[todayDayIndex].timings.Fajr.split()[0].split(':')
   def ZohrTime = JsonObject.data[todayDayIndex].timings.Dhuhr.split()[0].split(':')
   def AsrTime = JsonObject.data[todayDayIndex].timings.Asr.split()[0].split(':')
   def MaghrebTime = JsonObject.data[todayDayIndex].timings.Maghrib.split()[0].split(':')
   def IshaTime = JsonObject.data[todayDayIndex].timings.Isha.split()[0].split(':')
   def TomorrowFajrTime = GetTomorrowFajr(JsonObject.data)

   log.debug "FajrTime ${FajrTime}"
   log.debug "ZohrTime ${ZohrTime}"
   log.debug "AsrTime ${AsrTime}"
   log.debug "MaghrebTime ${MaghrebTime}"
   log.debug "IshaTime ${IshaTime}"
   log.debug "TomorrowFajrTime ${TomorrowFajrTime}"


   // get corresponding UTC for each pray           
   def todayFajrUTC = GetPrayerDateTimeInUTC(FajrTime[0], FajrTime[1])
   def todayZohrUTC = GetPrayerDateTimeInUTC(ZohrTime[0], ZohrTime[1])
   def todayAsrUTC = GetPrayerDateTimeInUTC(AsrTime[0], AsrTime[1])
   def todayMaghrebUTC = GetPrayerDateTimeInUTC(MaghrebTime[0], MaghrebTime[1])
   def todayIshaUTC = GetPrayerDateTimeInUTC(IshaTime[0], IshaTime[1])
   def TomorrowFajrUTC = GetPrayerDateTimeInUTC(TomorrowFajrTime[0], TomorrowFajrTime[1], true)
  

   // get seconds remaining for each pray
   def todayFajrSec = GetSecondsToPrayTime(todayFajrUTC);
   def tomorrowFajrSec = GetSecondsToPrayTime(TomorrowFajrUTC);

   def nextZohrInSec = GetSecondsToPrayTime(todayZohrUTC);
   def nextAsrInSec = GetSecondsToPrayTime(todayAsrUTC);
   def nextMaghrebInSec = GetSecondsToPrayTime(todayMaghrebUTC);
   def nextIshaInSec = GetSecondsToPrayTime(todayIshaUTC);
   def nextFajrSec = todayFajrSec > 0 ? todayFajrSec : tomorrowFajrSec
   
   if(IsRamadanModeActive == true)
       nextMaghrebInSec = nextMaghrebInSec - 35 

   // add positive seconds to list and get the minimum value

   def AllPrayersEvents = []

   if (nextFajrSec > 0)
    AllPrayersEvents.add(GetPrayerTimeObject("Fajr", nextFajrSec, FajrVolume,  FajrIsActive))

   if (nextZohrInSec > 0)
    AllPrayersEvents.add(GetPrayerTimeObject("Zohr", nextZohrInSec, ZoherVolume,  ZoherIsActive))

   if (nextAsrInSec > 0)
    AllPrayersEvents.add(GetPrayerTimeObject("Asr", nextAsrInSec, AsrVolume,  AsrIsActive))

   if (nextMaghrebInSec > 0)
    AllPrayersEvents.add(GetPrayerTimeObject("Maghreb", nextMaghrebInSec, MaghrebVolume,  MaghrebIsActive))

   if (nextIshaInSec > 0)
    AllPrayersEvents.add(GetPrayerTimeObject("Isha", nextIshaInSec, IsaVolume,  IsaIsActive))
   
  
   if(IsRamadanModeActive && EnableKidsMode)
   {
      try{
           def KidsIftarUTC = GetPrayerDateTimeInUTC(KidsIftarHour, "00")
           def nextKidsIftarInSec = GetSecondsToPrayTime(KidsIftarUTC);
           if (nextKidsIftarInSec > 0)
			   AllPrayersEvents.add(GetPrayerTimeObject("KidsIftar", nextKidsIftarInSec, KidsIftarVolume,  true))
      }
      catch (e) {
          sendPush("Kids Configuration not correct")
          log.error "Kids Configuration not correct"
      }
   }


   def nextPrayEvent = AllPrayersEvents.min {
    it.Time
   }

   log.debug "nextPrayTime ${nextPrayEvent.Time}"


   return nextPrayEvent
  }
 } catch (e) {
  log.error "something went wrong: $e"
 }
}

def GetPrayerTimeObject(name, time, volume,  isActive) {
 def obj = [Name: name, Time: time, Volume: volume, IsActive: isActive]
}

def GetKidsTimeAzan(){
   def todayIshaUTC = GetPrayerDateTimeInUTC(IshaTime[0], IshaTime[1])
   def nextIshaInSec = GetSecondsToPrayTime(todayIshaUTC);

}

def GetTargetDeviceByName(name) {

 if (name == "Fajr")
  return FajrTargets

 if (name == "Zohr")
  return ZoherTargets

 if (name == "Asr")
  return AsrTargets

 if (name == "Maghreb")
  return MaghrebTargets

 if (name == "Isha")
  return IsaTargets
  
 if (name == "KidsIftar")
  return KidsIftarTargets
}

def GetPlayBackUrlBySalahName(name){
  if (name == "Fajr")
     return "http://192.168.178.118/assets/Azan_Cairo.mp3"


 if (name == "Zohr")
  return "http://192.168.178.118/assets/Azan_Cairo.mp3"


 if (name == "Asr")
  return "http://192.168.178.118/assets/Azan_Cairo.mp3"


 if (name == "Maghreb")
    if(IsRamadanModeActive == true)
      return "http://192.168.178.118/assets/Ramadan.maghreb.mp3"
    else
      return "http://192.168.178.118/assets/Azan_Cairo.mp3"


 if (name == "Isha")
  return "http://192.168.178.118/assets/Azan_Cairo.mp3"
  
  
 if (name == "KidsIftar")
  return "http://192.168.178.118/assets/only_madfa3.mp3"
  
}

def GetTomorrowFajr(todayData) {

 def todatMonth = new Date().format('MM') as int
 def tomorrowDay = new Date().plus(1).format('dd') as int
 def tomorrowDayIndex = tomorrowDay -1 as int  // because the index in the array [in the api] starts with 0  so day 27 for example is in the 26th index 

 def tomorrowMonth = new Date().plus(1).format('MM') as int
 def tomorrowYear = new Date().plus(1).format('yyyy') as int

 if (todatMonth == tomorrowMonth) {
  def tomorrowFajrTime = todayData[tomorrowDayIndex].timings.Fajr.split()[0].split(':')

  return tomorrowFajrTime
 } else {
  def params = [
   uri: "http://api.aladhan.com/v1/calendar?latitude=47.9568123&longitude=7.7496747&method=2&month=${TomorrowMonth}&year=${tomorrowYear}",
   path: ""
  ]

  try {
   httpGet(params) {
    resp ->

     def outputJasonData = new groovy.json.JsonOutput().toJson(resp.data)
    def JsonObject = new groovy.json.JsonSlurper().parseText(outputJasonData)

    assert JsonObject instanceof Map
    assert JsonObject.data instanceof List
    assert JsonObject.data[tomorrowDayIndex] instanceof Map
    assert JsonObject.data[tomorrowDayIndex].timings instanceof Map

    def tomorrowFajrTime = JsonObject.data[tomorrowDayIndex].timings.Fajr.split()[0].split(':')
    return tomorrowFajrTime
   }
  } catch (e) {
   log.error "something went wrong: $e"
  }
 }
}

def GetPrayerDateTimeInUTC(prayHour, prayMinutes, isTomorrowPray = false) {

 def daysToAdd = isTomorrowPray ? 1 : 0
 def dtFormat = "yyyy-MM-dd'T'HH:mm:ssZ"
 def dtPrayFormat = dtFormat.replace("HH", prayHour as String).replace("mm", prayMinutes as String)
 def PrayDateInGermanyTime = new Date().plus(daysToAdd).format(dtPrayFormat, TimeZone.getTimeZone('Europe/Berlin'))
 assert PrayDateInGermanyTime instanceof String

 def PrayDateTimeInUTC = Date.parse(dtFormat, PrayDateInGermanyTime)
 assert PrayDateTimeInUTC instanceof Date


 return PrayDateTimeInUTC

}

def GetSecondsToPrayTime(PrayTimeInUTC) {
 long timeDiff
 long unxNow = new Date().getTime() / 1000
 long unxPrayTime = PrayTimeInUTC.getTime() / 1000

 timeDiff = unxPrayTime - unxNow

 return timeDiff
}