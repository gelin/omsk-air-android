#!/usr/bin/python3

from os import listdir
from os.path import realpath, dirname, basename, join
from PIL import Image


def load_parts(resolution):
    res_path = join(dirname(realpath(__file__)), resolution)
    result = {}
    for file in listdir(res_path):
        if file.endswith('.png'):
            image = Image.open(join(res_path, file))
            result[file[:-4]] = image
    return result


if __name__ == '__main__':
    parts = load_parts('hdpi')
    for (name, image) in parts.items():
        print(name, image.size)
