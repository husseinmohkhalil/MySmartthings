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
    name: "Azan",
    namespace: "husseinmohkhalil",
    author: "Hussein Khalil",
    description: "This is The Azaaaaan :)",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	 section("Which Virtual Dimmer You are going to use"){
    input(name: "virtualDimmer", type: "capability.switch", title: "Which switch?", required: true)
   }
   section("Which device(s) to Play Sound "){
    input(name: "targets", type: "capability.musicPlayer", title: "Target dimmer switch(s)", multiple: true, required: true)
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
   subscribe(virtualDimmer, "switch.on", SwitchOnHandler)
   
   
}


def SwitchOnHandler(evt) {
   	log.debug "Updated with settings: ${evt.value}"
   targets.setTrack("https://www.islamcan.com/audio/adhan/azan12.mp3");
}

def GetAzanTimes(){

    def todatDay = new Date().format( 'dd' ) as int 
    def todatMonth = new Date().format( 'MM' ) as int 
    def todatYear = new Date().format( 'yyyy' ) as int 
    
    def nextAzanTimeInSecounds
    
  	def params = [
 		   uri: "http://api.aladhan.com/v1/calendar?latitude=47.9568123&longitude=7.7496747&method=2&month=${todatMonth}&year=${todatYear}",
  		   path: ""
				]

	try {
    	httpGet(params) { resp ->
        	
            def outputJasonData = new groovy.json.JsonOutput().toJson(resp.data)
            def JsonObject    = new groovy.json.JsonSlurper().parseText(outputJasonData) 
        	
            assert JsonObject instanceof Map
            assert JsonObject.data instanceof List
            assert JsonObject.data[todatDay] instanceof Map
            assert JsonObject.data[todatDay].timings instanceof Map
                        
            def FajrTime = JsonObject.data[todatDay].timings.Fajr.split()[0].split(':')
            def ZohrTime = JsonObject.data[todatDay].timings.Dhuhr.split()[0].split(':')
            def AsrTime = JsonObject.data[todatDay].timings.Asr.split()[0].split(':')
            def MaghrebTime = JsonObject.data[todatDay].timings.Maghrib.split()[0].split(':')
            def IshaTime = JsonObject.data[todatDay].timings.Isha.split()[0].split(':')
            
            def Fajr = new Date().copyWith(
                                            hourOfDay: FajrTime[0] as int,
                                            minute: FajrTime[01] as int)
            def Zohr = new Date().copyWith(
                                            hourOfDay: ZohrTime[0] as int,
                                            minute: ZohrTime[01] as int)
            def Asr = new Date().copyWith(
                                            hourOfDay: AsrTime[0] as int,
                                            minute: AsrTime[01] as int)
            def Maghreb = new Date().copyWith(
                                            hourOfDay: MaghrebTime[0] as int,
                                            minute: MaghrebTime[01] as int)
            def Isha = new Date().copyWith(
                                            hourOfDay: IshaTime[0] as int,
                                            minute: IshaTime[01] as int)
                                            
            
            log.debug "Fajr  ${Fajr}"
            log.debug "Zohr  ${Zohr}"
            log.debug "Asr  ${Asr}"
            log.debug "Maghreb  ${Maghreb}"
            log.debug "Isha  ${Isha}"
	}
	} catch (e) {
        log.error "something went wrong: $e"
    }
}

 

def GetParayerDateTimeInUTC(prayHour,prayMinutes){
	
    def dtFormat = "yyyy-MM-dd'T'HH:mm:ssZ"
    def dtPrayFormat = dtFormat.replace("HH",prayHour as String).replace("mm",prayMinutes as String)
	def PrayDateInGermanyTime = new Date().format(dtPrayFormat, TimeZone.getTimeZone('Europe/Berlin'))
	assert PrayDateInGermanyTime instanceof String

	def PrayDateTimeInUTC = Date.parse(dtFormat , PrayDateInGermanyTime)
	assert PrayDateTimeInUTC instanceof Date
        log.debug "${PrayDateTimeInUTC}"

GutSecoundsToNextPrayTime(PrayDateTimeInUTC)

    return PrayDateTimeInUTC 
  
}

def GutSecoundsToNextPrayTime(nextPrayTimeInUTC)
{
	long timeDiff
    long unxNow = new Date().getTime()/1000
    long unxPrayTime = nextPrayTimeInUTC.getTime()/1000
    
    timeDiff = Math.abs(unxPrayTime - unxNow)
    timeDiff = Math.round(timeDiff/60)
    log.debug "${timeDiff}"
    
 }