package cmu.xprize.comp_numberscale;

import org.json.JSONObject;

import cmu.xprize.util.ILoadableObject;
import cmu.xprize.util.IScope;
import cmu.xprize.util.JSON_Helper;

/**
 * Automatically generated w/ script by Kevin DeLand.
 */

public class CNumberScale_Data implements ILoadableObject{

    // json loadable
    // insert C_Data fields... ${C_Data_fields}
    public String level;
    public String task;
    public String layout;
    public int[] dataset;


    @Override
    public void loadJSON(JSONObject jsonObj, IScope scope) {
        JSON_Helper.parseSelf(jsonObj, this, CClassMap.classMap, scope);
    }
}
