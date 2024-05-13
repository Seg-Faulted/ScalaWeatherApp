//> using dep com.softwaremill.sttp.client3::core:3.9.6
//> using dep com.softwaremill.sttp.client3::upickle:3.9.6
//> using dep org.postgresql:postgresql:42.7.3
//> using dep org.scala-lang.modules::scala-swing:3.0.0
import sttp.client3._
import scala.io.StdIn._
import sttp.client3.upicklejson._
import upickle.default._
import scala.collection.mutable
import scala.swing._
import java.sql.{
  Connection,
  DriverManager,
  Statement,
  SQLException,
  PreparedStatement
}

@main def gui() = {
  val frame = new Frame {
    title = "Hello world"
    
    contents = new FlowPanel {
      contents += new Label("Launch rainbows:")
      contents += new Button("Click me") {
        reactions += {
          case event.ButtonClicked(_) =>
            println("All the colours!")
        }
      }
    }
    pack()
    centerOnScreen()
    open()
  }
}

  def CheckIfCityStateCountryExists(
    CITY: String,
    STATE: String,
    COUNTRY: String,
    statement: Statement // Add this parameter
): Boolean = {
  val checkCITYSTATEQuery = s"""
  select
  count(*)
  from
  weatherlocations
  where
"  city = '$CITY'"
"  and state = '$STATE'"
"  and country = '$COUNTRY';"
  """
  val resultSet = statement.executeQuery(checkCITYSTATEQuery)
  resultSet.next()
  val count: Int = resultSet.getInt(1)
  if (count > 0) {
    println(
      s"$CITY, $STATE, $COUNTRY exists in the database.\nAttempting to fetch resources from the database."
    )
    val RetrieveGRIDS = s"""
    select
    gridx,
    gridy,
    forecastoffice
    from
    weatherlocations
    where
    city = '$CITY'
   and state = '$STATE'
    and country = '$COUNTRY';
    """
    val resultSet = statement.executeQuery(RetrieveGRIDS)
    resultSet.next()
    val gridXretrieved: String = resultSet.getString(1)
    val gridYretrieved: String = resultSet.getString(2)
    val FORECASTOFFICE: String = resultSet.getString(3)

    val NWSGRIDPOINTSRequest = basicRequest.get(
      uri"https://api.weather.gov/gridpoints/$FORECASTOFFICE/$gridXretrieved,$gridYretrieved/forecast"
    )
    val NWSBackend = HttpClientSyncBackend()
    val NWSResponse = NWSGRIDPOINTSRequest.send(NWSBackend)
    val NWSjsonData = NWSResponse.body match {
      case Right(body) => ujson.read(body)
      case Left(error) => throw new Exception(s"Failed to fetch data: $error")
    }

    val Temperature =
      NWSjsonData("properties")("periods")(0)("temperature").num.toString
    val TemperatureUnit =
      NWSjsonData("properties")("periods")(0)("temperatureUnit").str
    val Forecast =
      NWSjsonData("properties")("periods")(0)("shortForecast").str
    val Precipitation = NWSjsonData("properties")("periods")(0)(
      "probabilityOfPrecipitation"
    )("value").toString
    println(s"$Temperature")
    println(s"$TemperatureUnit")
    println(s"$Forecast")
    println(
      s"\nResults for Today: \nThe temperature is: $Temperature $TemperatureUnit.\n\nForecast: $Forecast\n The chance of precipitation is: $Precipitation%\n\n"
    )

    true
  } else {
    println(
      s"$CITY, $STATE, $COUNTRY" + "\n\n does not exist in the database.\nAttempting to build sources manually."
    )
    false
  }
}

def start_terminal(args: Array[String]): Unit = {
  val url =
    "jdbc:postgresql://aws-0-us-west-1.pooler.supabase.com:5432/postgres?user=postgres.lsjjxfyijphiafssvcrl&password=babySharkSupa"
  val username = "postgres.lsjjxfyijphiafssvcrl"
  val password = "babySharkSupa"

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
    println("Table is not empty.\n")
  } else {
    println("Table is empty.\n")
  }
  val exists = CheckIfCityStateCountryExists(CITY, STATE, COUNTRY, statement) // Pass statement here
  if (!exists) {
    GeoNameRequest(CITY, STATE, COUNTRY)
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
      NationalWeatherServiceGRIDPOINTSJsonData: ujson.Value,
      latitude: Double,
      longitude: Double,
      gridX: String,
      gridY: String,
      forecastOffice: String
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
    val Precipitation = NationalWeatherServiceGRIDPOINTSJsonData("properties")(
      "periods"
    )(0)("probabilityOfPrecipitation")("value").toString
    println(s"Today's weather forecast: $statusOfCurrentDay")
    println(s"Today's temperature: $temperature F")
    println(s"Today's precipitation: $Precipitation%")

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
    val InsertNewDataIntoDatabase =
         s"""insert into public.weatherlocations (city, state,country,latitude,longtitude,gridx,gridy,forecastoffice)
    values ('$CITY','$STATE','$COUNTRY','$latitude','$longitude','$gridX','$gridY','$forecastOffice')"""
    // print(s"$sqlInsertStatement")

    val resultSet = statement.executeQuery(InsertNewDataIntoDatabase)
    resultSet.next()
    connection.close()
  }
  }