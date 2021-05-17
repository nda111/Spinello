import time as tm

__prev_time__ = -1
__delta_time__ = -1


def __get_time__():
    return tm.time()


def init():
    global __prev_time__
    __prev_time__ = __get_time__()


def update():
    global __prev_time__, __delta_time__
    now = __get_time__()
    __delta_time__ = now - __prev_time__
    __prev_time__ = now


def get_delta():
    return __delta_time__
