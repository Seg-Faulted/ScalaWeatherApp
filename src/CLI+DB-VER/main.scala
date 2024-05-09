//> using dep com.softwaremill.sttp.client3::core:3.9.6
//> using dep com.softwaremill.sttp.client3::upickle:3.9.6
//> using dep org.postgresql:postgresql:42.7.3

import sttp.client3._
import scala.io.StdIn._
import sttp.client3.upicklejson._
import upickle.default._
import scala.collection.mutable
import java.sql.{Connection, DriverManager, Statement, SQLException}

def main(args: Array[String]): Unit = {
  val url =
    "jdbc:postgresql://aws-0-us-west-1.pooler.supabase.com:5432/postgres?user=postgres.lsjjxfyijphiafssvcrl&password=babySharkSupa"
  val username = "postgres.lsjjxfyijphiafssvcrl"
  val password = "babySharkSupa"

  val createTableQuery = """
    CREATE TABLE IF NOT EXISTS HelloWorld (
      id SERIAL PRIMARY KEY,
      message TEXT
    );
  """

  println("---------")
  val CITY = readLine("Enter city: ")
  val STATE = readLine("Enter state: ")
  val COUNTRY = readLine("Enter country: ")

  val connection: Connection =
    DriverManager.getConnection(url, username, password)
  val statement: Statement = connection.createStatement()

  val checkTableEmptyCompletely = s"""
  select
  count(*)
  from
  WeatherLocations;"""
  val resultSet = statement.executeQuery(checkTableEmptyCompletely)
  resultSet.next()
  val empty: Int = resultSet.getInt(1)
  if (empty > 0) {
    println("Table is not empty.")
  } else {
    println("Table is empty.")
  }
  CheckIfCityStateCountryExists(CITY, STATE, COUNTRY)
 

  def CheckIfCityStateCountryExists(
      
      CITY: String,
      STATE: String,
      COUNTRY: String,
    
  ): Unit = {
   
    val checkCITYSTATEQuery = s"""
    select
    count(*)
    from
    WeatherLocations
    where
    CITY = '$CITY'
    and STATE = '$STATE'
    and COUNTRY_CODE = '$COUNTRY';"""
    val resultSet = statement.executeQuery(checkCITYSTATEQuery)
    resultSet.next()
    val x: Int = resultSet.getInt(1)
    if (x > 0) {
      println("It exists in the database.")
    } else {
      println(s"$CITY, $STATE, $COUNTRY, does not exist in the database.")
    }
   
    program(CITY, STATE, COUNTRY)
  }

  def program(
  
      CITY: String,
      STATE: String,
      COUNTRY: String
  ): String = {  // Corrected return type from 'Stringt' to 'String'
    val USERNAME = "summitwxreq"  // Changed 'var' to 'val' as USERNAME does not change

    println("---------")
    val GeoNameRequest = basicRequest
      .get(
        uri"http://api.geonames.org/searchJSON?q=$CITY&state=$STATE&country=$COUNTRY&username=$USERNAME"  // Corrected URI string interpolation
      )
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
      NationalWeatherServiceLATLONGRequest.send(NationalWeatherServiceLATLONGBackend)
    val NWSjsonData = NationalWeatherServiceLATLONGResponse.body match {
      case Right(body) => ujson.read(body)
      case Left(error) => throw new Exception(s"Failed to fetch data: $error")
    }

    val forecastOffice = NWSjsonData("properties")("cwa").str  // Corrected to directly access string
    val gridX = NWSjsonData("properties")("gridX").num.toInt.toString  // Corrected to handle number conversion
    val gridY = NWSjsonData("properties")("gridY").num.toInt.toString  // Corrected to handle number conversion

    val NationalWeatherServiceGRIDPOINTSRequest = basicRequest
      .get(
        uri"https://api.weather.gov/gridpoints/$forecastOffice/$gridX,$gridY/forecast"
      )
    val NationalWeatherServiceGRIDPOINTSBackend = HttpClientSyncBackend()
    val NationalWeatherServiceGRIDPOINTSResponse =
      NationalWeatherServiceGRIDPOINTSRequest.send(NationalWeatherServiceGRIDPOINTSBackend)
    val NationalWeatherServiceGRIDPOINTSJsonData =
      NationalWeatherServiceGRIDPOINTSResponse.body match {
        case Right(body) => ujson.read(body)
        case Left(error) => throw new Exception(s"Failed to fetch data: $error")
      }
    val statusOfCurrentDay: String = NationalWeatherServiceGRIDPOINTSJsonData(
      "properties"
    )("periods")(0)("shortForecast").str
    println(s"Today's weather forecast: $statusOfCurrentDay")
    "Weather data fetched successfully"  // Added a return statement for the function
  }

}