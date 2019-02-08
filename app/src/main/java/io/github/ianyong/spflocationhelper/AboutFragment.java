package io.github.ianyong.spflocationhelper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

public class AboutFragment extends Fragment {

    private Toolbar toolbar;
    private TextView textBuildVersion;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Set up action bar.
        View v = inflater.inflate(R.layout.about_view, container, false);
        toolbar = v.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle("About");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        // Get build version.
        textBuildVersion = v.findViewById(R.id.about_build_version);
        textBuildVersion.setText(BuildConfig.VERSION_NAME);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

    }

}
