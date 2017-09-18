# Copyright (C) 2017 NXP

SUMMARY = "OPTEE test"
HOMEPAGE = "http://www.optee.org/"

LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://LICENSE.md;md5=daa2bcccc666345ab8940aab1315a4fa"

DEPENDS = "optee-os optee-client linux-optee-imx uboot-optee-imx"

SRC_URI = "git://sw-stash.freescale.net/scm/imx/imx-optee-test.git;branch=imx_2.5.y;protocol=http"
SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"

do_compile () {
    if [ ${MACHINE} = "imx8mqevk" ];then
        export TA_DEV_KIT_DIR=${STAGING_INCDIR}/optee/export-user_ta_arm64/
        export ARCH=arm64
    else
        export TA_DEV_KIT_DIR=${STAGING_INCDIR}/optee/export-user_ta_arm32/
        export ARCH=arm
    fi
    export OPTEE_CLIENT_EXPORT=${STAGING_DIR_HOST}/usr
    export CROSS_COMPILE_HOST=${HOST_PREFIX} \
    export CROSS_COMPILE_TA=${HOST_PREFIX} \
    export CROSS_COMPILE=${HOST_PREFIX}

    oe_runmake V=1
}

do_install () {
    install -d ${D}/usr/bin
    install ${S}/out/xtest/xtest ${D}/usr/bin/

    if [ ${MACHINE} = "imx8mqevk" ]; then
        install -d ${D}/lib64/optee_armtz
        find ${S}/out/ta -name '*.ta' | while read name; do
            install -m 444 $name ${D}/lib64/optee_armtz/
        done
    else
        install -d ${D}/lib/optee_armtz
        find ${S}/out/ta -name '*.ta' | while read name; do
            install -m 444 $name ${D}/lib/optee_armtz/
        done
    fi
}

FILES_${PN} = "/usr/bin/ /lib*/optee_armtz/"
