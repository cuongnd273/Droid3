/*
 * Copyright (c) 2012-2020 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.apero.openvpn.activities;

import android.content.Context;
import android.content.Intent;

public class VariantConfig {
    public static Intent getOpenUrlIntent(Context c) {
        return new Intent(Intent.ACTION_VIEW);
    }

}
