package com.augmos.iink.prototype;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.myscript.iink.Configuration;
import com.myscript.iink.ContentPackage;
import com.myscript.iink.ContentPart;
import com.myscript.iink.Editor;
import com.myscript.iink.Engine;
import com.myscript.iink.IEditorListener;
import com.myscript.iink.uireferenceimplementation.EditorView;
import com.myscript.iink.uireferenceimplementation.FontUtils;
import com.myscript.iink.uireferenceimplementation.InputController;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;


//Diese Klasse ist dafür da den Interactive Ink Editor dynamisch in einem Teilbereich des Bildschirms anzuzeigen

public class InteractiveInkFragment extends Fragment implements View.OnClickListener {

    private Engine engine;
    private ContentPackage contentPackage;
    private ContentPart contentPart;
    private EditorView editorView;
    private String TAG = "InteractiveInk Frame";

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        engine = IInkApplication.getEngine();
        // configure recognition
        Configuration conf = engine.getConfiguration();
        String confDir = "zip://" + getActivity().getPackageCodePath() + "!/assets/conf";
        conf.setStringArray("configuration-manager.search-path", new String[]{confDir});
        String tempDir = getActivity().getFilesDir().getPath() + File.separator + "tmp";
        conf.setString("content-package.temp-folder", tempDir);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_iink, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        editorView = getView().findViewById(R.id.editor_view);

        // load fonts
        AssetManager assetManager = getActivity().getApplicationContext().getAssets();
        Map<String, Typeface> typefaceMap = FontUtils.loadFontsFromAssets(assetManager);
        editorView.setTypefaces(typefaceMap);

        editorView.setEngine(engine);

        final Editor editor = editorView.getEditor();
        editor.addListener(new IEditorListener()
        {
            @Override
            public void partChanging(Editor editor, ContentPart oldPart, ContentPart newPart)
            {
                // no-op
            }

            @Override
            public void partChanged(Editor editor)
            {
                //invalidateOptionsMenu();
            }

            @Override
            public void contentChanged(Editor editor, String[] blockIds)
            {
                //invalidateOptionsMenu();
            }

            @Override
            public void onError(Editor editor, String blockId, String message)
            {
                Log.e(TAG, "Failed to edit block \"" + blockId + "\"" + message);
            }
        });

        setInputMode(InputController.INPUT_MODE_FORCE_PEN); // If using an active pen, put INPUT_MODE_AUTO here

        String packageName = UUID.randomUUID().toString();
        File file = new File(getActivity().getApplicationContext().getFilesDir(), packageName);
        try
        {
            contentPackage = engine.createPackage(file);
            contentPart = contentPackage.createPart("Math"); // Choose type of content (possible values are: "Text Document", "Text", "Diagram", "Math", and "Drawing")
        }
        catch (IOException e)
        {
            Log.e(TAG, "Failed to open package \"" + packageName + "\"", e);
        }
        catch (IllegalArgumentException e)
        {
            Log.e(TAG, "Failed to open package \"" + packageName + "\"", e);
        }

        //setTitle("Aufgabe: " + "Dolen");

        // wait for view size initialization before setting part
        editorView.post(new Runnable()
        {
            @Override
            public void run()
            {
                editorView.getRenderer().setViewOffset(0, 0);
                editorView.getRenderer().setViewScale(1);
                editorView.setVisibility(View.VISIBLE);
                editor.setPart(contentPart);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        editorView.setOnTouchListener(null);
        editorView.close();

        if (contentPart != null)
        {
            contentPart.close();
            contentPart = null;
        }
        if (contentPackage != null)
        {
            contentPackage.close();
            contentPackage = null;
        }


        // IInkApplication has the ownership, do not close here
        engine = null;

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {


        super.onDestroy();
    }


    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.button_input_mode_forcePen:
                setInputMode(InputController.INPUT_MODE_FORCE_PEN);
                break;
            case R.id.button_input_mode_forceTouch:
                setInputMode(InputController.INPUT_MODE_FORCE_TOUCH);
                break;
            case R.id.button_input_mode_auto:
                setInputMode(InputController.INPUT_MODE_AUTO);
                break;
            case R.id.button_undo:
                editorView.getEditor().undo();
                break;
            case R.id.button_redo:
                editorView.getEditor().redo();
                break;
            case R.id.button_clear:
                editorView.getEditor().clear();
                break;
            default:
                Log.e(TAG, "Failed to handle click event");
                break;
        }
    }

    private void setInputMode(int inputMode)
    {
        editorView.setInputMode(inputMode);
    }

    public ContentPart getContentPart(){
        return contentPart;
    }

}
