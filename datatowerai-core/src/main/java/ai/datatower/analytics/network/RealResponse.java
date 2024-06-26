/*
 * Created by chenru on 2020/06/22.
 * Copyright 2015－2020 Sensors Data Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.datatower.analytics.network;

import androidx.annotation.NonNull;

import java.util.Locale;

public class RealResponse {
    public String result;
    public String errorMsg;
    public String location;
    public int code;
    public long contentLength;
    public long date;
    public Exception exception;

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "code:%d\nresult:%s\nlocation:%s\nerrorMsg:%s\nexception:%s",
                code, result, location, errorMsg,
                exception == null ? "" : exception.getMessage());
    }
}