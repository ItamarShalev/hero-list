package hero.apps.exam_hero_apps.thread;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hero.apps.exam_hero_apps.global.Global;


/**
 /**
 * this function create new thread , and this thread get new object from json from server
 */

public class CheckingObjectUrlThread<T,H> extends Thread {


    private final String JSON_URL;
    @Nullable
    private String lastJson;
    private OnNewObjectListListener<T,H> newObjectListListener;
    private List<T> objectList;
    private ParseJsonToObject<T> parseJsonToObject;
    private List<T> newObjectList;
    private WeakReference<H> referenceTag;
    private String response;

    /**
     * @param JSON_URL the url for json only if is jsonArray and all json object inside is object
     * @param lastJson for Less objects creates, memory and comparisons can be null
     * @param objectList the list you want equals and get only new object from json, nullable if you want get all object from json
     * @param newObjectListListener listener when finish with new objects, when finish without new objects and when error
     * @param tag Some object you want get after the process finish
     * @param parseJsonToObject how to get new Object with jsonObject
     */
    public CheckingObjectUrlThread(String JSON_URL, @Nullable String lastJson,@Nullable List<T> objectList, @NonNull OnNewObjectListListener<T,H> newObjectListListener, ParseJsonToObject<T> parseJsonToObject, H tag) {
        this.JSON_URL = JSON_URL;
        this.lastJson = lastJson;
        this.objectList = objectList;
        this.newObjectListListener = newObjectListListener;
        this.parseJsonToObject = parseJsonToObject;
        this.referenceTag = new WeakReference<>(tag);
        newObjectList = new ArrayList<>();
    }


    public interface OnNewObjectListListener<T,H>{
        void onFound(List<T> newObjectList,H tag,String jsonResult);
        void onFinishSearch(H tag);
        void onError(StateNetworkingError stateNetworkingError,H tag);
    }

    public enum StateNetworkingError{
        NO_HAVE_INTERNET, INTERNET_SLOW,ERROR_SERVER,ERROR_JSON
    }

    public interface ParseJsonToObject<T> {
        public T getObjectFromJson(JSONObject jsonObject);
    }


    @Override
    public void run() {
        StateNetworkingError stateNetworkingError = null;
        try {
            response = Global.NetworkingUtils.getHttpGetString(JSON_URL, 3000);
            if (lastJson == null || !lastJson.equals(response)) {
                JSONArray jsonArray = new JSONArray(response);
                JSONObject jsonObject;
                T newObj;
                Set<T> hashSet = null;
                if (objectList == null || objectList.size() == 0) {
                    hashSet = new HashSet<>(objectList);
                }
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    newObj = parseJsonToObject.getObjectFromJson(jsonObject);
                    if (hashSet == null || !hashSet.contains(newObj)) {
                        newObjectList.add(newObj);
                    }
                }

            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (newObjectList.size() > 0) {
                        newObjectListListener.onFound(newObjectList,referenceTag.get(),response);
                    } else {
                        newObjectListListener.onFinishSearch(referenceTag.get());
                    }
                }
            });
        } catch (JSONException e) {
            stateNetworkingError = StateNetworkingError.ERROR_JSON;
        } catch (UnknownHostException e) {
            stateNetworkingError = StateNetworkingError.NO_HAVE_INTERNET;
        } catch (SocketTimeoutException e) {
            stateNetworkingError = StateNetworkingError.INTERNET_SLOW;
        } catch (IOException e) {
            stateNetworkingError = StateNetworkingError.ERROR_SERVER;
        }
        if (stateNetworkingError != null) {
            newObjectListListener.onError(stateNetworkingError, referenceTag.get());
        }
    }
}