package erni;

import erni.betterask.hiking.client.model.RouteSegment;
import erni.betterask.openapi.resttemplate.test.OpenApiValidator;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.configurators.ArbitraryConfigurator;
import net.jqwik.api.domains.Domain;
import net.jqwik.api.domains.DomainContext;
import net.jqwik.api.providers.ArbitraryProvider;
import net.jqwik.api.providers.TypeUsage;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class RouteSegmentTest {

    OpenApiValidator validator = new OpenApiValidator("target/generated-sources/openapi/api/openapi.yaml", RouteSegmentTest.class);

    @Property
    @Domain(RouteSegmentDomain.class)
    void testIsValid(@ForAll RouteSegment routeSegment) {

        validator.validate(routeSegment);
    }

    // inherits from Global context
    @Domain(DomainContext.Global.class)
    public static class RouteSegmentDomain implements DomainContext {

        @Override
        public Collection<ArbitraryProvider> getArbitraryProviders() {

            return List.of(

                    new ArbitraryProvider() {

                        @Override
                        public boolean canProvideFor(TypeUsage targetType) {
                            return targetType.isOfType(RouteSegment.class);
                        }

                        @Override
                        public Set<Arbitrary<?>> provideFor(TypeUsage targetType, SubtypeProvider subtypeProvider) {
                            return Set.of(
                                    Arbitraries.of(RouteSegment.builder().build())
                            );
                        }
                    }
            );
        }

        @Override
        public Collection<ArbitraryConfigurator> getArbitraryConfigurators() {
            return List.of();
        }
    }
}
