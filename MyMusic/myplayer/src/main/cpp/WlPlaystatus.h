//
// Created by lenovo on 2020/4/25.
//

#ifndef MYMUSIC_WLPLAYSTATUS_H
#define MYMUSIC_WLPLAYSTATUS_H


class WlPlaystatus {

public:
    bool exit = false;
    bool load = true;

    bool seek = false;

    bool pause = false;

public:
    WlPlaystatus();
    ~WlPlaystatus();

};


#endif //MYMUSIC_WLPLAYSTATUS_H
