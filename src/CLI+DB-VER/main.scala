//> using dep com.softwaremill.sttp.client3::core:3.9.6
//> using dep com.softwaremill.sttp.client3::upickle:3.9.6
//> using dep org.postgresql:postgresql:42.7.3

import sttp.client3._
import scala.io.StdIn._
import sttp.client3.upicklejson._
import upickle.default._
import scala.collection.mutable
import java.sql.{Connection, DriverManager, Statement, SQLException, PreparedStatement}

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
  val CITY = readLine("Enter city: ").toUpperCase()
  val STATE = readLine("Enter state: ").toUpperCase()
  val COUNTRY = readLine("Enter country: ").toUpperCase()

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
      COUNTRY: String
  ): Unit = {

    val checkCITYSTATEQuery = s"""
    select
    count(*)
    from
    weatherlocations
    where
    city = '$CITY'
    and state = '$STATE'
    and country = '$COUNTRY';
    """
    val resultSet = statement.executeQuery(checkCITYSTATEQuery)
    resultSet.next()
    val x: Int = resultSet.getInt(1)
    if (x > 0) {
      println(s"$CITY, $STATE, $COUNTRY exists in the database.")
    } else {
      println(
        s"$CITY, $STATE, $COUNTRY" + "\n\n does not exist in the database, so, it will attempt to fetch it manually."
      )
      GeoNameRequest(CITY, STATE, COUNTRY)
    }

  }

  def GeoNameRequest(
      CITY: String,
      STATE: String,
      COUNTRY: String
  ): String = { // Corrected return type from 'Stringt' to 'String'
    val USERNAME =
      "summitwxreq" // Changed 'var' to 'val' as USERNAME does not change

    println("---------")
    val GeoNameRequest = basicRequest
      .get(
        uri"http://api.geonames.org/searchJSON?q=$CITY&state=$STATE&country=$COUNTRY&username=$USERNAME" // Corrected URI string interpolation
      )
    val GeoNameBackend = HttpClientSyncBackend()
    val GeoNameResponse = GeoNameRequest.send(GeoNameBackend)
    val GEONAMEjsonData = GeoNameResponse.body match {
      case Right(body) => ujson.read(body)
      case Left(error) => throw new Exception(s"Failed to fetch data: $error")
    }
    val latitude = GEONAMEjsonData("geonames")(0)("lat").str.toDouble
    val longitude = GEONAMEjsonData("geonames")(0)("lng").str.toDouble

    NWSLATLONGRequest(latitude, longitude)
  }
  def NWSLATLONGRequest(
      latitude: Double,
      longitude: Double
  ): String = {
    val NationalWeatherServiceLATLONGRequest =
      basicRequest.get(uri"https://api.weather.gov/points/$latitude,$longitude")
    val NationalWeatherServiceLATLONGBackend = HttpClientSyncBackend()
    val NationalWeatherServiceLATLONGResponse =
      NationalWeatherServiceLATLONGRequest.send(
        NationalWeatherServiceLATLONGBackend
      )
    val NWSjsonData = NationalWeatherServiceLATLONGResponse.body match {
      case Right(body) => ujson.read(body)
      case Left(error) => throw new Exception(s"Failed to fetch data: $error")
    }

    val forecastOffice =
      NWSjsonData("properties")("cwa").str
    val gridX =
      NWSjsonData("properties")("gridX").num.toInt.toString
    val gridY =
      NWSjsonData("properties")("gridY").num.toInt.toString

    s"Forecast Office: $forecastOffice, GridX: $gridX, GridY: $gridY"
    NWSGRIDPOINTSRequest(
      forecastOffice: String,
      gridX: String,
      gridY: String,
      latitude: Double,
      longitude: Double,
      gridX: String,
      gridY: String
    )
  }
  def NWSGRIDPOINTSRequest(
      forecastOffice: String,
      gridX: String,
      gridY: String,
      latitude: Double,
      longitude: Double,
      gridX$1: String,
      gridY$1: String
  ): String = {
    val NationalWeatherServiceGRIDPOINTSRequest = basicRequest
      .get(
        uri"https://api.weather.gov/gridpoints/$forecastOffice/$gridX,$gridY/forecast"
      )
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

    "Data fetched successfully"
    NWSweatherRequest(
      NationalWeatherServiceGRIDPOINTSJsonData,
      latitude,
      longitude,
      gridX,
      gridY,
      forecastOffice
    )
  }
  def NWSweatherRequest(
      NationalWeatherServiceGRIDPOINTSJsonData: ujson.Value,
      latitude: Double,
      longitude: Double,
      gridX: String,
      gridY: String,
      forecastOffice: String
  ): String = {
    val statusOfCurrentDay: String = NationalWeatherServiceGRIDPOINTSJsonData(
      "properties"
    )("periods")(0)("shortForecast").str
    val temperature = NationalWeatherServiceGRIDPOINTSJsonData("properties")(
      "periods"
    )(0)("temperature").num.toInt
    println(s"Today's weather forecast: $statusOfCurrentDay")
    println(s"Today's temperature: $temperature")

    AddToDatabase(
      CITY,
      STATE,
      COUNTRY,
      latitude,
      longitude,
      gridX,
      gridY,
      forecastOffice,
      url,
      username,
      password
    )
    "test"
  }

  def AddToDatabase(
      CITY: String,
      STATE: String,
      COUNTRY: String,
      latitude: Double,
      longitude: Double,
      gridX: String,
      gridY: String,
      forecastOffice: String,
      url: String,
      username: String,
      password: String
  ): Unit = {
    val connection: Connection =
      DriverManager.getConnection(url, username, password)
    val statement: Statement = connection.createStatement()


  
    val sqlInsertStatement = s"""insert into public.weatherlocations (city, state,country,latitude,longtitude,gridx,gridy,forecastoffice)
    values ('$CITY','$STATE','$COUNTRY','$latitude','$longitude','$gridX','$gridY','$forecastOffice')"""
  //print(s"$sqlInsertStatement")
  
 
    val resultSet = statement.executeQuery(sqlInsertStatement)
    resultSet.next()
   // val x: Int = resultSet.getInt(1)
       connection.close()
       System.exit(0)
  }
}
