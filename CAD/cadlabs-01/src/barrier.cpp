//
// Created by Hervé Paulino on 07/03/18.
//

#include <barrier.h>


namespace cad {

    barrier::barrier(std::size_t count) :
            _initial_count(count),
            _count(_initial_count)
    { }


    void  barrier::wait_for_all() {
        std::unique_lock <std::mutex> lock(_mutex);

        if (--_count == 0) {
            _count = _initial_count;
            _cv.notify_all();
        } else {
            _cv.wait(lock, [this] { return _count == _initial_count; });
        }
    }

}