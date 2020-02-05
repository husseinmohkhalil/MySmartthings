/**
 *  test Smart app
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
    name: "test Smart app",
    namespace: "husseinmohkhalil",
    author: "Hussein Khalil",
    description: "only for testing and wala 7aga ",
    category: "",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Title") {
		// TODO: put inputs here
	}
}

def installed() {
	//log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	//log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {

   makeJSONWeatherRequest()
	// TODO: subscribe to attributes, devices, locations, etc.
}

def makeJSONWeatherRequest() { 

    def todatDay = new Date().format( 'dd' ) as int 
    def todatMonth = new Date().format( 'MM' ) as int 
    def todatYear = new Date().format( 'yyyy' ) as int
    def nowDateTime = new Date()
    log.debug "${nowDateTime}"
    def newDate = GetParayerDateTime(23,35)
    
  	def params = [
 		   uri: "http://api.aladhan.com/v1/calendar?latitude=47.9568123&longitude=7.7496747&method=2&month=1&year=2020",
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
            
            
            
           // log.debug "Fajr  ${Fajr}"
            
            
	}
	 		} catch (e) {
        log.error "something went wrong: $e"
         }
}

def GetParayerDateTime(prayHour,prayMinutes){
	
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
 // def nowDate = new Date()
  //def duration = groovy.time.TimeCategory.minus(nowDate, nextPrayTimeInUTC)
          log.debug "${timeDiff}"
          
          
          ///kedah bi7seb lel3adi ... enama lw al fagr lazem ykon al fagr fi al yourm al gedeed

}

