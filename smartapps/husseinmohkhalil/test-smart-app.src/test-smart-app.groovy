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
                        
            def Fajr = JsonObject.data[todatDay].timings.Fajr.split()[0]
            def Zohr = JsonObject.data[todatDay].timings.Dhuhr.split()[0]
            def Asr = JsonObject.data[todatDay].timings.Asr.split()[0]
            def Maghreb = JsonObject.data[todatDay].timings.Maghrib.split()[0]
            def Isha = JsonObject.data[todatDay].timings.Isha.split()[0]
            
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

// TODO: implement event handlers