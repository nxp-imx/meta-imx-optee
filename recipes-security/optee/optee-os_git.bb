# Copyright (C) 2017 NXP

SUMMARY = "OPTEE OS"
DESCRIPTION = "OPTEE OS"
HOMEPAGE = "http://www.optee.org/"
LICENSE = "BSD"
LIC_FILES_CHKSUM = "file://LICENSE;md5=69663ab153298557a59c67a60a743e5b"

inherit deploy pythonnative autotools
DEPENDS = "python-pycrypto-native python-wand-native"
SRC_URI = "git://bitbucket.sw.nxp.com/scm/imx/imx-optee-os.git;branch=imx_2.5.y;protocol=http"
SRCREV = "${AUTOREV}"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build.${OPTEE_PLATFORM}"

python () {
	machine = d.getVar("MACHINE", True)

	import re
	if re.match('imx',machine):
		subplatform = machine[1:]
	else:
        	bb.fatal("optee-os doesn't recognize this MACHINE")
	d.setVar("OPTEE_PLATFORM", subplatform)
}

OPTEE_ARCH ?= "arm32"
OPTEE_ARCH_armv7a = "arm32"
OPTEE_ARCH_aarch64 = "arm64"


EXTRA_OEMAKE = "PLATFORM=imx PLATFORM_FLAVOR=${OPTEE_PLATFORM} \
                CROSS_COMPILE=${HOST_PREFIX} \
                NOWERROR=1 \
                LDFLAGS= \
		O=${B} \
        "

do_compile () {
    unset LDFLAGS
    export CFLAGS="${CFLAGS} --sysroot=${STAGING_DIR_HOST}"
    oe_runmake -C ${S} all CFG_TEE_TA_LOG_LEVEL=0
}


do_mkimage () {
   install -d ${DEPLOY_DIR_IMAGE}
   ${TARGET_PREFIX}objcopy -O binary ${B}/core/tee.elf ${DEPLOY_DIR_IMAGE}/tee.${OPTEE_PLATFORM}.bin

   IMX_LOAD_ADDR=`cat ${B}/core/tee-init_load_addr.txt` && \
   ${S}/mkimage -A arm -O linux -C none -a ${IMX_LOAD_ADDR} -e ${IMX_LOAD_ADDR} \
	-d ${DEPLOY_DIR_IMAGE}/tee.${OPTEE_PLATFORM}.bin ${DEPLOY_DIR_IMAGE}/uTee-${OPTEE_BIN_EXT}
}


do_install () {
    install -d ${D}/lib/firmware/
    install -m 644 ${B}/core/*.bin ${D}/lib/firmware/

    # Install the TA devkit
    install -d ${D}/usr/include/optee/export-user_ta_${OPTEE_ARCH}/

    for f in ${B}/export-ta_${OPTEE_ARCH}/*; do
        cp -aR $f ${D}/usr/include/optee/export-user_ta_${OPTEE_ARCH}/
    done
}

addtask do_mkimage after do_compile before do_install


FILES_${PN} = "/lib/firmware/"
FILES_${PN}-dev = "/usr/include/optee"
INSANE_SKIP_${PN}-dev = "staticdev"

COMPATIBLE_MACHINE = "(mx6|mx7|mx8m)"
