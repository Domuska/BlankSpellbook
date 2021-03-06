package tomi.piipposoft.blankspellbook.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import tomi.piipposoft.blankspellbook.R;

/**
 * Created by OMISTAJA on 7.6.2017.
 */

public class Helper {

    public static int getRandomColorFromString(String name){

        //Log.d(TAG, "name got in getRandomColorFromString: " + name);
        String color = String.format("#%X", name.hashCode());
        //Log.d(TAG, "name in hex:" + color);
        int red = Integer.valueOf( color.substring( 1, 3 ), 16 );
        int green = Integer.valueOf( color.substring( 3, 5 ), 16 );
        int blue = Integer.valueOf( color.substring( 5, 7 ), 16 );
        //Log.d(TAG, "red: " + red + " green:" + green +  " blue: " + blue);

        //tint the random color with white to get pastel colors
        red = (red + 255) / 2;
        green = (green + 255) / 2;
        blue = (blue + 255) / 2;

        return Color.rgb(red, green, blue);


        //https://stackoverflow.com/questions/43044/algorithm-to-randomly-generate-an-aesthetically-pleasing-color-palette
        //generate a random color
        //should we define ranges to avoid certain colours?
        /*Random random = new Random();
        int red = random.nextInt(256);
        int green = random.nextInt(256);
        int blue = random.nextInt(256);*/

        //use some color to mix the random color to get a tint, here lightBlue is used
        /*red = (red + 173) / 2;
        green = (green + 216) / 2;
        blue = (blue + 230) / 2;*/

        //orangeRed
        /*red = (red + 255) / 2;
        green = (green + 69) / 2;
        blue = (blue + 0) / 2;*/

        //light orange
        /*red = (red + 255) / 2;
        green = (green + 106) / 2;
        blue = (blue + 50) / 2;*/

        //green
        /*red = (red + 153) / 2;
        green = (green + 204) / 2;
        blue = (blue + 153) / 2;*/
    }

    public static int getAccentColor(Context context) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorAccent });
        int color = a.getColor(0, 0);

        a.recycle();

        return color;
    }

    /**
     * Helper method for changing the search view colour. Ugly but it should work even if Google
     * decides to change the ID of the search view.
     * @param view
     */
    public static void changeSearchViewTextColor(View view, Context c) {
        //get the primary colour from styles. It has to be done in this hard way...? Really?
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = c.getTheme();
        theme.resolveAttribute(android.R.attr.textColorPrimaryInverse, typedValue, true);
        TypedArray arr =
                c.obtainStyledAttributes(typedValue.data, new int[]{
                        android.R.attr.textColorPrimaryInverse});
        int textColor = arr.getColor(0, -1);
        arr.recycle();

        if (view != null) {
            //if we get TextView (the user's inputted text goes in one), set the text colour
            if (view instanceof TextView) {
                //this does not work because... Why?
                /*((TextView) view).setTextColor(
                        ContextCompat.getColor(c, R.color.my_color_dark_gray));*/
                ((TextView) view).setTextColor(textColor);
                return;
            } else if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    changeSearchViewTextColor(viewGroup.getChildAt(i), c);
                }
            }
        }
    }
}
