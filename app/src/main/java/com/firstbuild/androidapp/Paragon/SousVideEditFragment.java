package com.firstbuild.androidapp.paragon;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.firstbuild.androidapp.R;
import com.firstbuild.androidapp.paragon.dataModel.RecipeInfo;
import com.firstbuild.androidapp.paragon.dataModel.RecipeManager;
import com.firstbuild.androidapp.paragon.dataModel.StageInfo;
import com.firstbuild.androidapp.viewUtil.SwipeMenu;
import com.firstbuild.androidapp.viewUtil.SwipeMenuCreator;
import com.firstbuild.androidapp.viewUtil.SwipeMenuItem;
import com.firstbuild.androidapp.viewUtil.SwipeMenuListView;

/**
 * Created by Hollis on 11/4/15.
 */
public class SousVideEditFragment extends Fragment {

    private String TAG = RecipeEditFragment.class.getSimpleName();

    private EditText editIngredients;
    private EditText editDirections;
    private SwipeMenuListView stageListView;
    private RadioGroup groupDetail;

    private EditText editName;
    private ParagonMainActivity attached = null;


    public SousVideEditFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        attached = (ParagonMainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_soudvide_edit, container, false);

        editName = (EditText) view.findViewById(R.id.edit_name);

        editIngredients = (EditText) view.findViewById(R.id.edit_ingredients);
        editDirections = (EditText) view.findViewById(R.id.edit_directions);
        groupDetail = (RadioGroup) view.findViewById(R.id.group_recipe_detail);

        groupDetail.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_ingredients:
                        editIngredients.setVisibility(View.VISIBLE);
                        editDirections.setVisibility(View.GONE);
//                        stageListView.setVisibility(View.GONE);
                        break;

                    case R.id.radio_directions:
                        editIngredients.setVisibility(View.GONE);
                        editDirections.setVisibility(View.VISIBLE);
//                        stageListView.setVisibility(View.GONE);
                        break;

                    case R.id.radio_settings:
                        attached.nextStep(ParagonMainActivity.ParagonSteps.STEP_ADD_SOUSVIDE_SETTING);
                        break;
                }
            }
        });

        groupDetail.check(R.id.radio_ingredients);


        view.findViewById(R.id.btn_cook).setVisibility(View.GONE);
        view.findViewById(R.id.btn_save).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

            }
        });


        getCurrentRecipe();


        return view;
    }

    private void getCurrentRecipe() {
        RecipeInfo recipe = RecipeManager.getInstance().getCurrentRecipe();

        StageInfo stageAdd = new StageInfo(0, 0, 0, false, "");
        stageAdd.setType(StageInfo.TYPE_ADD_ITEM);
        recipe.addStage(stageAdd);
    }



}