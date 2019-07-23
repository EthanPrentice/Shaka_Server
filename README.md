# Shaka_Server
Server / API for Shaka, which acts as a shared queue that Spotify users can contribute to
Jul-2019 - Not happy with current implementation.


**I've learned a lot in past year and there are many areas I can improve this, especially design-wise.  
I will be deprecating this and starting writing a C++ API late Jul-2019 instead to practice / demonstrate knowledge of architecture, design patterns, and pointers/object ownership in a personal project.**


# TODO
Currently only uses polling and should be refactored to use sockets instead.

Needs to be documented with javadoc.

Implement shuffling ability.

Change way the queue is implemented to be more similar to Spotify's queue for consistency.
