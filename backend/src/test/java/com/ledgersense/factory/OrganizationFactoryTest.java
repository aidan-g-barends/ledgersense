package com.ledgersense.factory;

import com.ledgersense.domain.Organization;
import org.junit.jupiter.api.Test;

import static java.lang.Character.getName;
import static org.junit.jupiter.api.Assertions.*;

class OrganizationFactoryTest {

    @Test
    void createOrganization() {
        Organization org = OrganizationFactory.createOrganization("Digital Solution WC", "ZAR");

        assertEquals("Digital Solution WC", org.getName());
        assertEquals("ZAR", String.valueOf(org.getBaseCurrency()));
    }

    @Test
    void createOrganizationWhenNameNull(){
        Organization org1 = OrganizationFactory.createOrganization(null , "ZAR");

        assertNull(org1);
    }

    @Test
    void createOrganizationWhenCurrencyInvalid(){
        Organization org2 = OrganizationFactory.createOrganization("Digital Solutions WC", "rand");

        assertNull(org2);
    }
}