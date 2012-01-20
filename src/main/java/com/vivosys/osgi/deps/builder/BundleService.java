package com.vivosys.osgi.deps.builder;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;

public class BundleService {

    private Bundle bundle;
    private ServiceReference serviceReference;

    public BundleService(Bundle bundle, ServiceReference serviceReference) {
        this.bundle = bundle;
        this.serviceReference = serviceReference;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BundleService that = (BundleService) o;

        if (!bundle.equals(that.bundle)) return false;
        if (!serviceReference.equals(that.serviceReference)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = bundle.hashCode();
        result = 31 * result + serviceReference.hashCode();
        return result;
    }
}
