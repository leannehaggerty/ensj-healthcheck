/*
 * Copyright [1999-2016] Wellcome Trust Sanger Institute and the EMBL-European Bioinformatics Institute
 * 
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
 */

package org.ensembl.healthcheck.util;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Provides a similar function as {@link AbstractStringMapRowMapper} but returns
 * the first column as the key value but as a Long. This same method can be used
 * for int/Integer based result sets
 *
 * @author ayates
 * @author dstaines
 */
public abstract class AbstractLongMapRowMapper<T> extends
		AbstractMapRowMapper<Long, T> {

	/**
	 * Returns the first column as the keyed value & is a Long
	 */
	public Long getKey(ResultSet resultSet) throws SQLException {
		return resultSet.getLong(1);
	}

}
