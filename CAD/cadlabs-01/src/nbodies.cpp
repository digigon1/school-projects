/**
 *
 * Implementation of class nbodies
 */

#include <fstream>
#include <cmath>


#include <nbodies.h>
#include <thread>

namespace cad {

    nbodies::nbodies(const std::string &filename, const unsigned num_iterations) :
            _num_iterations(num_iterations) {
        parse_input(filename);
    }


    void nbodies::parse_input(const std::string &inputFile) {
        std::ifstream in;
        in.open(inputFile);
        if (!in.good())
            throw std::runtime_error("File " + inputFile + " does not exist. ");

        in >> _num_bodies;
        _position.reserve(_num_bodies * 4);
        _velocity.reserve(_num_bodies * 4);


        for (int i = 0; i < _num_bodies; i++) {
            int index = 4 * i;
            // First 3 values are _position in x,y and z direction
            // First 3 values are _velocity in x,y and z direction
            for (int j = 0; j < 3; ++j) {
                in >> _position[index + j];
                _velocity[index + j] = 0.0f;
            }
            // Mass value
            in >> _position[index + 3];
            // unused
            _velocity[3] = 0.0f;
        }
    }

    void nbodies::run() {
        const float delT = 0.005f;
        const float espSqr = 50.0f;

        std::vector<float> new_position;
        std::vector<float> new_velocity;

        for (unsigned n = 0; n < _num_iterations; n++) {
            float acc[3] = {0.0f, 0.0f, 0.0f};

            //Iterate for all samples
            int NThreads = 8;
            std::thread threads[NThreads];

            for (int t = 0; t < NThreads; t++) {
                threads[t] = std::thread ([this, NThreads, espSqr, &acc, &new_position, delT, &new_velocity, t] {
                    int th_size = _num_bodies / NThreads;
                    for (int i = t*th_size; i < (t+1)*th_size; ++i) {
                        int myIndex = 4 * i;

                        for (unsigned j = 0; j < _num_bodies; ++j) {
                            unsigned index = 4 * j;
                            float r[3];
                            float distSqr = espSqr;

                            for (int k = 0; k < 3; ++k) {
                                r[k] = _position[index + k] - _position[myIndex + k];
                                distSqr += r[k] * r[k];
                            }

                            float invDist = 1.0f / std::sqrt(distSqr);
                            float invDistCube = invDist * invDist * invDist;
                            float s = _position[index + 3] * invDistCube;

                            for (unsigned k = 0; k < 3; ++k) {
                                acc[k] += s * r[k];
                            }
                        }

                        for (unsigned k = 0; k < 3; ++k) {
                            new_position[myIndex + k] += _velocity[myIndex + k] * delT + 0.5f * acc[k] * delT * delT;
                            new_velocity[myIndex + k] += acc[k] * delT;
                        }
                    }
                });
            }

            for (int i = 0; i < NThreads; i++) {
                threads[i].join();
            }
        }

        _position = new_position;
        _velocity = new_velocity;
    }
}