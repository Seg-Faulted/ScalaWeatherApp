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
  println("---------")
  var CITY = readLine("Enter city: ")
  var STATE = readLine("Enter state: ")
  var COUNTRY = readLine("Enter country: ")
  println("---------")
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
    basicRequest.get(uri"https://api.weather.gov/points/$lat,$long")
  val NationalWeatherServiceLATLONGBackend = HttpClientSyncBackend()
  val NationalWeatherServiceLATLONGResponse =
    NationalWeatherServiceLATLONGRequest.send(
      NationalWeatherServiceLATLONGBackend
    )

  val NWSjsonData = NationalWeatherServiceLATLONGResponse.body match {
    case Right(body) => ujson.read(body)
    case Left(error) => throw new Exception(s"Failed to fetch data: $error")

    /////////
  }

  // println(NationalWeatherServiceLATLONGResponse.body)
  val forecastOffice =
    NWSjsonData("properties")("cwa").toString.filterNot("\"".toSet)
  val gridX = NWSjsonData("properties")("gridX").toString
  val gridY = NWSjsonData("properties")("gridY").toString
  val properties = NWSjsonData("properties")
  val toRemove = "\" ".toSet

  val NationalWeatherServiceGRIDPOINTSRequest = basicRequest
    .get(
      uri"https://api.weather.gov/gridpoints/$forecastOffice/$gridX,$gridY/forecast"
    )

  // this sends a request to the GeoNames API and parses the response to JSON
  val NationalWeatherServiceGRIDPOINTSBackend = HttpClientSyncBackend()
  val NationalWeatherServiceGRIDPOINTSResponse =
    NationalWeatherServiceGRIDPOINTSRequest.send(
      NationalWeatherServiceGRIDPOINTSBackend
    )
  val NationalWeatherServiceGRIDPOINTSJsonData =
    NationalWeatherServiceGRIDPOINTSResponse.body match {
      case Right(body) => ujson.read(body)
      case Left(error) => throw new Exception(s"Failed to fetch data: $error")
    }
  val statusOfCurrentDay: String = NationalWeatherServiceGRIDPOINTSJsonData(
    "properties"
  )("periods")(0)("name").toString.filterNot("\"".toSet)
  val windDirection: String = NationalWeatherServiceGRIDPOINTSJsonData(
    "properties"
  )("periods")(0)("windDirection").toString.filterNot("\"".toSet)
  val windSpeed: String = NationalWeatherServiceGRIDPOINTSJsonData(
    "properties"
  )("periods")(0)("windSpeed").toString.filterNot("\"".toSet)
  val temperature: String = NationalWeatherServiceGRIDPOINTSJsonData(
    "properties"
  )("periods")(0)("temperature").toString.filterNot("\"".toSet)
  val tempUnits: String = NationalWeatherServiceGRIDPOINTSJsonData(
    "properties"
  )("periods")(0)("temperatureUnit").toString.filterNot("\"".toSet)

  println("---------")
  println(statusOfCurrentDay)
  println("Temperature: " + temperature + " " + tempUnits)
  println("Wind: " + windDirection + " " + windSpeed + "")
  println("---------")
  // println(NationalWeatherServiceGRIDPOINTSResponse)
  // println()
  // println(Temperature + TemperatureUnit)

  // println(GeoNameRNationalWeatherServiceGRIDPOINTSResponse)
  // todo: parse weatherStation response to JSON, and then print temps, etc
end Program
