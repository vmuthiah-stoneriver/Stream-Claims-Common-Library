package com.client.iip.integration.financials.disbursement;

import java.util.Map;

import com.fiserv.isd.iip.core.data.query.rdbms.Criteria;
import com.fiserv.isd.iip.core.data.query.source.QueryBuilderDefault;
import com.fiserv.isd.iip.core.meta.annotation.QueryBuilder;
import com.stoneriver.iip.claims.financial.ClaimParticipantSearchCriteria;

@QueryBuilder(id = "client.claim.financials..retrieveClaimParticipantsQueryBuilder")
public class ClientClaimParticipantsQueryBuilder extends QueryBuilderDefault{

	private static final String SELECT_STATEMENT = "selectClause";

	private static final String JOIN_STATEMENT = "commonJoin";


	/**
	 * {@inheritDoc}
	 */
	@Override
	public String buildSQL(Criteria queryCriteria,
			Map<String, String> sqlFragments, String drl) {

		StringBuilder query = null;

		if (!(queryCriteria instanceof ClaimParticipantSearchCriteria)) {
			throw new IllegalArgumentException("expecting a "
					+ ClaimParticipantSearchCriteria.class);
		}

		query = new StringBuilder();
		query.append(sqlFragments.get(SELECT_STATEMENT));
		query.append(sqlFragments.get(JOIN_STATEMENT));

		return query.toString();
	}
}

