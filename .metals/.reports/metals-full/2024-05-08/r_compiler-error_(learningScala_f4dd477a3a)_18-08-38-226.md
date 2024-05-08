file://<WORKSPACE>/src/main.scala
### java.lang.AssertionError: assertion failed

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
offset: 1201
uri: file://<WORKSPACE>/src/main.scala
text:
```scala
//> using dep com.softwaremill.sttp.client3::core:3.9.6
//> using dep com.softwaremill.sttp.client3::upickle:3.9.6

import sttp.client3._
import scala.io.StdIn._
import sttp.client3.upicklejson._
import upickle.default._
import scala.collection.mutable
// TODO: SPLIT UP REQUESTS, AND PARSES INTO FUNCTIONS.
@main def Program =
  var USERNAME = "summitwxreq"

  var CITY = readLine("Enter city: ")
  var STATE = readLine("Enter state: ")
  var COUNTRY = readLine("Enter country: ")

  val GeoNameRequest = basicRequest
    .get(
      uri"http://api.geonames.org/searchJSON?q=$CITY&state=$STATE&country=$COUNTRY&username=summitwxreq"
    )
  // this sends a request to the GeoNames API and parses the response to JSON
  val GeoNameBackend = HttpClientSyncBackend()
  val GeoNameResponse = GeoNameRequest.send(GeoNameBackend)
  val GEONAMEjsonData = GeoNameResponse.body match {
    case Right(body) => ujson.read(body)
    case Left(error) => throw new Exception(s"Failed to fetch data: $error")
  }
  val lat = GEONAMEjsonData("geonames")(0)("lat").str.toDouble
  val long = GEONAMEjsonData("geonames")(0)("lng").str.toDouble

  val NationalWeatherServiceLATLONGRequest =
    basicRequest.get(uri"htt@@ps://api.weather.gov/points/$lat,$long")
  val NationalWeatherServiceLATLONGBackend = HttpClientSyncBackend()
  val NationalWeatherServiceLATLONGResponse =
    NationalWeatherServiceLATLONGRequest.send(NationalWeatherServiceLATLONGBackend)

  val NWSjsonData = NationalWeatherServiceLATLONGResponse.body match {
    case Right(body) => ujson.read(body)
    case Left(error) => throw new Exception(s"Failed to fetch data: $error")
  }

  //println(NationalWeatherServiceLATLONGResponse.body)
  val forecastOffice = NWSjsonData("properties")("cwa").toString
  val gridX = NWSjsonData("properties")("gridX").toString
  val gridY = NWSjsonData("properties")("gridY").toString
  val properties = NWSjsonData("properties")
  val toRemove = "\" ".toSet
  val forecastOfficeFiltered = forecastOffice.filterNot(toRemove)

  val NationalWeatherServiceGRIDPOINTSRequest = basicRequest
    .get(
      uri"https://api.weather.gov/gridpoints/$forecastOfficeFiltered/$gridX,$gridY/forecast"
    )
 
  // this sends a request to the GeoNames API and parses the response to JSON
  val NationalWeatherServiceGRIDPOINTSBackend = HttpClientSyncBackend()
  val NationalWeatherServiceGRIDPOINTSResponse = NationalWeatherServiceGRIDPOINTSRequest.send(NationalWeatherServiceGRIDPOINTSBackend)
  var Temperature = NWSjsonData("properties")//("").toString.toDouble
  //var TemperatureUnit = NWSjsonData("properties")("temperatureUnit").toString
 // print(NationalWeatherServiceGRIDPOINTSRequest)
  println(NWSjsonData("properties"))
  //println()
  //println(Temperature + TemperatureUnit)

  //println(GeoNameRNationalWeatherServiceGRIDPOINTSResponse)
  // todo: parse weatherStation response to JSON, and then print temps, etc
end Program

```



#### Error stacktrace:

```
scala.runtime.Scala3RunTime$.assertFailed(Scala3RunTime.scala:11)
	dotty.tools.dotc.core.Contexts$FreshContext.setOwner(Contexts.scala:621)
	dotty.tools.dotc.core.Contexts$Context.exprContext(Contexts.scala:450)
	dotty.tools.dotc.interactive.Interactive$.contextOfStat(Interactive.scala:272)
	dotty.tools.dotc.interactive.Interactive$.contextOfPath(Interactive.scala:315)
	dotty.tools.dotc.interactive.Interactive$.contextOfPath(Interactive.scala:283)
	dotty.tools.dotc.interactive.Interactive$.contextOfPath(Interactive.scala:283)
	dotty.tools.dotc.interactive.Interactive$.contextOfPath(Interactive.scala:283)
	dotty.tools.dotc.interactive.Interactive$.contextOfPath(Interactive.scala:283)
	dotty.tools.dotc.interactive.Interactive$.contextOfPath(Interactive.scala:283)
	dotty.tools.pc.utils.MtagsEnrichments$.localContext(MtagsEnrichments.scala:72)
	dotty.tools.pc.PcDefinitionProvider.ctx$lzyINIT1$1(PcDefinitionProvider.scala:51)
	dotty.tools.pc.PcDefinitionProvider.ctx$6(PcDefinitionProvider.scala:51)
	dotty.tools.pc.PcDefinitionProvider.definitions(PcDefinitionProvider.scala:52)
	dotty.tools.pc.PcDefinitionProvider.definitions(PcDefinitionProvider.scala:33)
	dotty.tools.pc.ScalaPresentationCompiler.definition$$anonfun$1(ScalaPresentationCompiler.scala:153)
```
#### Short summary: 

java.lang.AssertionError: assertion failed