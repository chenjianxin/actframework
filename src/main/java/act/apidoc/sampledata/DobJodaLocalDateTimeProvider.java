package act.apidoc.sampledata;

/*-
 * #%L
 * ACT Framework
 * %%
 * Copyright (C) 2014 - 2018 ActFramework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import act.apidoc.SampleData;
import act.apidoc.SampleDataCategory;
import act.apidoc.SampleDataProvider;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@SampleData.Category(SampleDataCategory.DOB)
public class DobJodaLocalDateTimeProvider extends SampleDataProvider<LocalDateTime> {

    @Inject
    private DobStringProvider provider;

    private DateTimeFormatter format = DateTimeFormat.forPattern("yyyy-MM-dd");

    @Override
    public LocalDateTime get() {
        return format.parseLocalDateTime(provider.get());
    }
}
