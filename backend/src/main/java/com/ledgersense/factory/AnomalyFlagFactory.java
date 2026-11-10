package com.ledgersense.factory;

import com.ledgersense.domain.AnomalyFlag;
import com.ledgersense.domain.FlagEvidence;
import com.ledgersense.domain.FlagRule;
import com.ledgersense.domain.FlagSeverity;
import com.ledgersense.util.Helper;

import java.util.List;
import java.util.UUID;

public class AnomalyFlagFactory {

    // WHY a cap: the UI shows evidence in a small grid. More than 6 lines = too much to read.
    static final int MAX_EVIDENCE = 6;

    public static AnomalyFlag createAnomalyFlag(UUID orgId, UUID transactionId, FlagRule rule,
                                                FlagSeverity severity, String title, String explanation,
                                                List<FlagEvidence> evidence) {
        if (orgId == null || transactionId == null || rule == null || severity == null) {
            return null;
        }
        if (Helper.isNullOrBlank(title) || !Helper.isWithinLength(title.strip(), 100)) {
            return null;
        }
        if (Helper.isNullOrBlank(explanation) || !Helper.isWithinLength(explanation.strip(), 500)) {
            return null;
        }
        if (evidence == null || evidence.size() > MAX_EVIDENCE) {
            return null;
        }
        for (FlagEvidence item : evidence) {
            if (item == null
                    || Helper.isNullOrBlank(item.getLabel()) || !Helper.isWithinLength(item.getLabel(), 50)
                    || Helper.isNullOrBlank(item.getValue()) || !Helper.isWithinLength(item.getValue(), 100)) {
                return null;
            }
        }

        return new AnomalyFlag.Builder()
                .setOrgId(orgId)
                .setTransactionId(transactionId)
                .setRule(rule)
                .setSeverity(severity)
                .setTitle(title.strip())
                .setExplanation(explanation.strip())
                .setEvidence(evidence)
                .build();
    }
}