package io.github.ianyong.spfdivisionalboundaries;

import android.location.Location;
import android.os.Parcel;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

public class LocationSuggestion implements SearchSuggestion {
    private String location;
    private boolean isHistory = false;

    public LocationSuggestion(String suggestion){
        this.location = suggestion.toLowerCase();
    }

    public LocationSuggestion(Parcel source){
        this.location = source.readString();
        this.isHistory = source.readInt() != 0;
    }

    public void setIsHistory(boolean isHistory){
        this.isHistory = isHistory;
    }

    public boolean getIsHistory(){
        return isHistory;
    }

    public String getBody(){
        return location;
    }

    public static final Creator<LocationSuggestion> CREATOR = new Creator<LocationSuggestion>(){
        public LocationSuggestion createFromParcel(Parcel in){
            return new LocationSuggestion(in);
        }

        public LocationSuggestion[] newArray(int size){
            return new LocationSuggestion[size];
        }
    };

    public int describeContents(){
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags){
        dest.writeString(location);
        dest.writeInt(isHistory ? 1 : 0);
    }
}
