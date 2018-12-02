SUMMARY = "The VCU TRD design for AVNET's xczu7ev-fbvb900-1-i board"
DESCRIPTION = "Contains the VCU TRD enablement Files and hardware software \
hand-off file. The HDF provides bitstream and the ps7_init_gpl.c/h \
platform headers."
SECTION = "bsp"

DEPENDS += "unzip-native"

LICENSE = "Proprietary"

COMPATIBLE_MACHINE = "xczu7ev-fbvb900-1-i"


SRC_URI = "git://github.com/Brainiarc7/zcu7ev-vcu-trd.git;protocol=https;nobranch=1"
SRCREV = "AUTOINC"

PV = "+git${SRCPV}"

S = "${WORKDIR}/git"

HDF = "git://github.com/Brainiarc7/zcu7ev-vcu-trd/designs/vcu_trd/vcu_trd.sdk/vcu_trd_wrapper.hdf"

S ?= "${WORKDIR}/${MACHINE}"

PROVIDES = "virtual/bitstream virtual/xilinx-platform-init"

FILES_${PN}-platform-init += "${PLATFORM_INIT_DIR}/*"

FILES_${PN}-bitstream += " \
		download.bit \
		"

PACKAGES = "${PN}-platform-init ${PN}-bitstream"

BITSTREAM ?= "bitstream-${PV}-${PR}.bit"

PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit xilinx-platform-init
inherit deploy

SYSROOT_DIRS += "${PLATFORM_INIT_DIR}"

do_install() {
	fn=$(unzip -l ${S}/${HDF} | awk '{print $NF}' | grep ".bit$")
	unzip -o ${S}/${HDF} ${fn} -d ${D}
	[ "${fn}" == "download.bit" ] || mv ${D}/${fn} ${D}/download.bit

	install -d ${D}${PLATFORM_INIT_DIR}
	for fn in ${PLATFORM_INIT_FILES}; do
		unzip -o ${S}/${HDF} ${fn} -d ${D}${PLATFORM_INIT_DIR}
	done
}

do_deploy () {
	if [ -e ${D}/download.bit ]; then
		install -d ${DEPLOYDIR}
		install -m 0644 ${D}/download.bit ${DEPLOYDIR}/${BITSTREAM}
		ln -sf ${BITSTREAM} ${DEPLOYDIR}/download.bit
		# for u-boot 2016.3 with spl load bitstream patch
		ln -sf ${BITSTREAM} ${DEPLOYDIR}/bitstream
	fi
}
addtask deploy before do_build after do_install
