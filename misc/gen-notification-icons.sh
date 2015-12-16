#!/bin/sh

font=Droid-Sans-Bold

xxxhdpi_params="96x96 46 drawable-xxxhdpi"
xxhdpi_params="72x72 31 drawable-xxhdpi"
xhdpi_params="48x48 22 drawable-xhdpi"
hdpi_params="36x36 17 drawable-hdpi"
mdpi_params="24x24 11 drawable"
ldpi_params="18x18 8 drawable-ldpi"

gen_image() {
    img_size=$1
    point_size=$2
    res_folder=$3
    text=$4
    file_suffix=$5

    echo "Generating res/$res_folder/temp_$file_suffix.png"

    convert -size $img_size xc:transparent \
        -font $font \
        -gravity center -pointsize $point_size \
        -fill white -stroke none -annotate 0 "$text" \
        res/$res_folder/temp_$file_suffix.png
}

#-50 - -10
t=50
while [ $t -ge 10 ]
do
    gen_image $xxxhdpi_params "-$t°" minus_$t
    gen_image $xxhdpi_params "-$t°" minus_$t
    gen_image $xhdpi_params "-$t°" minus_$t
    gen_image $hdpi_params "-$t°" minus_$t
    gen_image $mdpi_params "-$t°" minus_$t
    gen_image $ldpi_params "-$t°" minus_$t
    #echo $t
    t=$(expr $t - 1)
done

#-9 - -1
t=9
while [ $t -ge 1 ]
do
    gen_image $xxxhdpi_params "-$t°" minus_$t
    gen_image $xxhdpi_params "-$t°" minus_$t
    gen_image $xhdpi_params "-$t°" minus_$t
    gen_image $hdpi_params "-$t°" minus_$t
    gen_image $mdpi_params "-$t°" minus_$t
    gen_image $ldpi_params "-$t°" minus_$t
    #echo $t
    t=$(expr $t - 1)
done

#0
    gen_image $xxxhdpi_params "0°" 0
    gen_image $xxhdpi_params "0°" 0
    gen_image $xhdpi_params "0°" 0
    gen_image $hdpi_params "0°" 0
    gen_image $mdpi_params "0°" 0
    gen_image $ldpi_params "0°" 0

#1 - 9
t=1
while [ $t -le 9 ]
do
    gen_image $xxxhdpi_params "+$t°" plus_$t
    gen_image $xxhdpi_params "+$t°" plus_$t
    gen_image $xhdpi_params "+$t°" plus_$t
    gen_image $hdpi_params "+$t°" plus_$t
    gen_image $mdpi_params "+$t°" plus_$t
    gen_image $ldpi_params "+$t°" plus_$t
    #echo $t
    t=$(expr $t + 1)
done

#10 - 50
t=10
while [ $t -le 50 ]
do
    gen_image $xxxhdpi_params "+$t°" plus_$t
    gen_image $xxhdpi_params "+$t°" plus_$t
    gen_image $xhdpi_params "+$t°" plus_$t
    gen_image $hdpi_params "+$t°" plus_$t
    gen_image $mdpi_params "+$t°" plus_$t
    gen_image $ldpi_params "+$t°" plus_$t
    #echo $t
    t=$(expr $t + 1)
done
