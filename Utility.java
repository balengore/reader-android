package com.example.balen.reader;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.balen.reader.data.LinkContract;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by balen on 8/7/14.
 */
public class Utility {

  public static String formatServerDate(String date) {
//    System.out.println(date);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    TimeZone utc = TimeZone.getTimeZone("UTC");
    sdf.setTimeZone(utc);
    Date testDate = null;

    try {
      testDate = sdf.parse(date);
    }catch(Exception ex){
      ex.printStackTrace();
    }

    SimpleDateFormat formatter = new SimpleDateFormat("hh:mma EEE");
    TimeZone pstTimeZone = TimeZone.getTimeZone("America/Los_Angeles");
    formatter.setTimeZone(pstTimeZone);
    String newFormat = formatter.format(testDate);

//    System.out.println(".....Date..." + newFormat);
    return newFormat;
  }

  /**
   * Helper method to provide the icon resource id according to the weather condition id returned
   * by the OpenWeatherMap call.
   * @param weatherId from OpenWeatherMap API response
   * @return resource id for the corresponding icon. -1 if no relation is found.
   */
//  public static int getIconResourceForWeatherCondition(int weatherId) {
//    // Based on weather code data found at:
//    // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
//    if (weatherId >= 200 && weatherId <= 232) {
//      return R.drawable.ic_storm;
//    } else if (weatherId >= 300 && weatherId <= 321) {
//      return R.drawable.ic_light_rain;
//    } else if (weatherId >= 500 && weatherId <= 504) {
//      return R.drawable.ic_rain;
//    } else if (weatherId == 511) {
//      return R.drawable.ic_snow;
//    } else if (weatherId >= 520 && weatherId <= 531) {
//      return R.drawable.ic_rain;
//    } else if (weatherId >= 600 && weatherId <= 622) {
//      return R.drawable.ic_snow;
//    } else if (weatherId >= 701 && weatherId <= 761) {
//      return R.drawable.ic_fog;
//    } else if (weatherId == 761 || weatherId == 781) {
//      return R.drawable.ic_storm;
//    } else if (weatherId == 800) {
//      return R.drawable.ic_clear;
//    } else if (weatherId == 801) {
//      return R.drawable.ic_light_clouds;
//    } else if (weatherId >= 802 && weatherId <= 804) {
//      return R.drawable.ic_cloudy;
//    }
//    return -1;
//  }

  /**
   * Helper method to provide the art resource id according to the weather condition id returned
   * by the OpenWeatherMap call.
   * @param weatherId from OpenWeatherMap API response
   * @return resource id for the corresponding image. -1 if no relation is found.
   */
//  public static int getArtResourceForWeatherCondition(int weatherId) {
//    // Based on weather code data found at:
//    // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
//    if (weatherId >= 200 && weatherId <= 232) {
//      return R.drawable.art_storm;
//    } else if (weatherId >= 300 && weatherId <= 321) {
//      return R.drawable.art_light_rain;
//    } else if (weatherId >= 500 && weatherId <= 504) {
//      return R.drawable.art_rain;
//    } else if (weatherId == 511) {
//      return R.drawable.art_snow;
//    } else if (weatherId >= 520 && weatherId <= 531) {
//      return R.drawable.art_rain;
//    } else if (weatherId >= 600 && weatherId <= 622) {
//      return R.drawable.art_rain;
//    } else if (weatherId >= 701 && weatherId <= 761) {
//      return R.drawable.art_fog;
//    } else if (weatherId == 761 || weatherId == 781) {
//      return R.drawable.art_storm;
//    } else if (weatherId == 800) {
//      return R.drawable.art_clear;
//    } else if (weatherId == 801) {
//      return R.drawable.art_light_clouds;
//    } else if (weatherId >= 802 && weatherId <= 804) {
//      return R.drawable.art_clouds;
//    }
//    return -1;
//  }
}
