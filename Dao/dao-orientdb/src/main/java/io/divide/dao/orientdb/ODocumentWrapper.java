/*
 * Copyright (C) 2014 Divide.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.divide.dao.orientdb;

import io.divide.shared.util.ReflectionUtils;
import io.divide.shared.transitory.TransientObject;
import com.orientechnologies.orient.core.index.OIndexFactory;
import com.orientechnologies.orient.core.index.OIndexes;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static io.divide.dao.orientdb.OIndexHighLander.ID;

public class ODocumentWrapper extends ODocument {

    public static final String indexAttribute = "index";

    protected ODocumentWrapper(){
        this.setAllowChainedAccess(true);
        this.setLazyLoad(false);
    }

    public ODocumentWrapper(ODocument doc){
        this();
        doc.copyTo(this);
    }

    public <B extends TransientObject> ODocumentWrapper(B b){
        super();

        String className = b.getObjectType();
        this.setAllowChainedAccess(true);
        this.setLazyLoad(false);


        // dirty unreliable hack to add HighLanderIndexFactory to the list.
        if(!OIndexes.getIndexTypes().contains(ID))
        try {
            HighLanderIndexFactory f = new HighLanderIndexFactory();
            Set<OIndexFactory> set = new HashSet<OIndexFactory>();
            Iterator<OIndexFactory> ite = OIndexes.getAllFactories();
            while (ite.hasNext()) {
                set.add(ite.next());
            }
            set.add(f);
            ReflectionUtils.setFinalStatic(ReflectionUtils.getClassField(OIndexes.class, "FACTORIES"), set);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create OrientDBWrapper");
        }

        if(getDatabase().getClusterIdByName(className) == -1){
            OClass object = getDatabase().getMetadata().getSchema().getOrCreateClass(className);
            object.createProperty(indexAttribute, OType.STRING)
                    .setMandatory(true)
                    .setNotNull(true)
                    .setReadonly(true);
            object.createIndex(className, ID, indexAttribute);
            getDatabase().getMetadata().getSchema().save();
        }

        Map user = b.getUserData();
        Map meta = b.getMetaData();

        field(indexAttribute, b.getObjectKey(), OType.STRING);
        field("user_data", user);
        field("meta_data", meta);
        super.setClassNameIfExists(className);

        System.out.println("DB: " + getDatabase().getName() + " : " + this.getClassName() + " : " + getDatabase().getClusterIdByName(className));
    }

    public String getKey(){
        return field("meta_data." + TransientObject.OBJECT_KEY);
    }

    public static <B extends TransientObject> B toObject(ODocument doc, Class<B> type){
        ODocumentWrapper w = new ODocumentWrapper(doc);
        return w.toObject(type);
    }

    public <B extends TransientObject> B toObject(Class<B> type){
        try {
            Constructor<B> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            B b = constructor.newInstance();

            Map user_data = (Map) ReflectionUtils.getObjectField(b, TransientObject.USER_DATA);
            Map meta_data = (Map) ReflectionUtils.getObjectField(b, TransientObject.META_DATA);
            user_data.clear();
            meta_data.clear();

            ReflectionUtils.setObjectField(b,TransientObject.USER_DATA,field(TransientObject.USER_DATA));
            ReflectionUtils.setObjectField(b, TransientObject.META_DATA, field(TransientObject.META_DATA));

            return b;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
