/*
 *
 *  Header with auxiliary functions for the test suites
 *
 */

#ifndef CADLABS_CAD_TEST
#define CADLABS_CAD_TEST

#include <gtest/gtest.h>

namespace cad {

    static const std::string data_path = "../test/data/";

    /**
     * Assert that all elements of a container equal a given value
     */
    template<typename Container, typename T>
    inline void expect_container_value(Container &c, T value) {

        for (unsigned int i = 0; i < c.size(); i++)
            EXPECT_EQ(value, c[i]);

    }

    template<typename Container, typename T>
    inline void expect_container_value(Container* c, T value, std::size_t size) {

        for (unsigned int i = 0; i < size; i++)
            EXPECT_EQ(value, c[i]);

    }

    /**
     * Assert that the contents of two containers are the same
     */
    template<typename Container>
    inline void expect_container_eq(Container &a, Container &&b) {

        EXPECT_EQ(a.size(), b.size());
        for (std::size_t i = 0; i < a.size(); i++)
            EXPECT_EQ(a.data()[i], b.data()[i]);
    }
}

#endif // CADLABS_CAD_TEST