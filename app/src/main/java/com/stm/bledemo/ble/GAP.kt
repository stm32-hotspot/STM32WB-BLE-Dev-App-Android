package com.stm.bledemo.ble

@Suppress("unused")
enum class GAP (
    val typeValue: String,
    val typeName: String,
    val typeFullName: String
){
    FLAGS       ("01", "Flags", "Flags"),
    IL16UUID    ("02", "IL16UUID", "Incomplete List of 16-bit Service Class UUIDs"),
    CL16UUID    ("03", "CL16UUID", "Complete List of 16-bit Service Class UUIDs"),
    IL32UUID    ("04", "IL32UUID", "Incomplete List of 32-bit Service Class UUIDs"),
    CL32UUID    ("05", "CL32UUID", "Complete List of 32-bit Service Class UUIDs"),
    IL128UUID   ("06", "IL128UUID", "Incomplete List of 128-bit Service Class UUIDs"),
    CL128UUID   ("07", "CL128UUID", "Complete List of 128-bit Service Class UUIDs"),
    SLNAME      ("08", "SLName", "Shortened Local Name"),
    CLNAME      ("09", "CLName", "Complete Local Name"),
    TXPOWER     ("0A", "Tx Power", "Tx Power Level"),
    DCLASS      ("0D", "Class", "Class of Device"),
    SPHASH      ("0E", "SPHash", "Simple Pairing Hash C"),
    SPRAND      ("0F", "SPRand", "Simple Pairing Randomizer R"),
    DID         ("10", "ID", "Device ID"),
    SMOBFLAGS   ("11", "SMOBFlags", "Security Manager Out of Band Flags"),
    SCIRANGE    ("12", "SCIRange", "Slave ConnectionInterface Interval Range"),
    L16SUUID    ("14", "L16SUUID", "List of 16-bit Service Solicitation UUIDs"),
    L128SUUID   ("15", "L128SUUID", "List of 128-bit Service Solicitation UUIDs"),
    SDATA       ("16", "SData", "Service Data"),
    PTADRESS    ("17", "PT Address", "Public Target Address"),
    RTADRESS    ("18", "RT Address", "Random Target Address"),
    APPEARANCE  ("19", "Appearance", "Appearance"),
    ADINTERVAL  ("1A", "Ad Interval", "Advertising Interval"),
    BLEDADDRESS ("1B", "BLED Address", "LE Bluetooth Device Address"),
    LEROLE      ("1C", "LE Role", "LE Role"),
    SPHASH256   ("1D", "SPHash-256", "Simple Pairing Hash C-256"),
    SPRAND256   ("1E", "SPRand-256", "Simple Pairing Randomizer R-256"),
    L32SUUID    ("1F", "L32SUUID", "List of 32-bit Service Solicitation UUIDs"),
    SDATA32     ("20", "SData-32", "Service Data - 32-bit UUID"),
    SDATA128    ("21", "SData-128", "Service Data - 128-bit UUID"),
    SCCVALUE    ("22", "SCCValue", "LE Secure Connections Confirmation Value"),
    SCRVALUE    ("23", "SCRValue", "LE Secure Connections Random Value"),
    URI         ("24", "URI", "URI"),
    INPOS       ("25", "In Position", "Indoor Positioning"),
    TDDATA      ("26", "TDData", "Transport Discovery Data"),
    LESFEAT     ("27", "LESFeatures", "LE Supported Features"),
    CMAPUP      ("28", "CMapUpdate", "Channel Map Update Indication"),
    PBADV       ("29", "PB-ADV", "PB-ADV"),
    MMESSAGE    ("2A", "MMessage", "Mesh Message"),
    MBEACON     ("2B", "MBeacon", "Mesh Beacon"),
    BIGINFO     ("2C", "BIGInfo", "BIGInfo"),
    BCCODE      ("2D", "BCCode", "Broadcast_Code"),
    RSID        ("2E", "RSID", "Resolvable Set Identifier"),
    ADINTL      ("2F", "Ad Int-long", "Advertising Interval - long"),
    DATA3D      ("3D", "3D Data", "3D Information Data"),
    MFR         ("FF", "MFR", "Manufacturer Specific Data");

    companion object {
        private val gapMap = mapOf(
            Pair(FLAGS.typeValue, FLAGS),
            Pair(IL16UUID.typeValue, IL16UUID),
            Pair(CL16UUID.typeValue, CL16UUID),
            Pair(IL32UUID.typeValue, IL32UUID),
            Pair(CL32UUID.typeValue, CL32UUID),
            Pair(IL128UUID.typeValue, IL128UUID),
            Pair(CL128UUID.typeValue, CL128UUID),
            Pair(SLNAME.typeValue, SLNAME),
            Pair(CLNAME.typeValue, CLNAME),
            Pair(TXPOWER.typeValue, TXPOWER),
            Pair(DCLASS.typeValue, DCLASS),
            Pair(SPHASH.typeValue, SPHASH),
            Pair(SPRAND.typeValue, SPRAND),
            Pair(DID.typeValue, DID),
            Pair(SMOBFLAGS.typeValue, SMOBFLAGS),
            Pair(SCIRANGE.typeValue, SCIRANGE),
            Pair(L16SUUID.typeValue, L16SUUID),
            Pair(L128SUUID.typeValue, L128SUUID),
            Pair(SDATA.typeValue, SDATA),
            Pair(PTADRESS.typeValue, PTADRESS),
            Pair(RTADRESS.typeValue, RTADRESS),
            Pair(APPEARANCE.typeValue, APPEARANCE),
            Pair(ADINTERVAL.typeValue, ADINTERVAL),
            Pair(BLEDADDRESS.typeValue, BLEDADDRESS),
            Pair(LEROLE.typeValue, LEROLE),
            Pair(SPHASH256.typeValue, SPHASH256),
            Pair(SPRAND256.typeValue, SPRAND256),
            Pair(L32SUUID.typeValue, L32SUUID),
            Pair(SDATA32.typeValue, SDATA32),
            Pair(SDATA128.typeValue, SDATA128),
            Pair(SCCVALUE.typeValue, SCCVALUE),
            Pair(SCRVALUE.typeValue, SCRVALUE),
            Pair(URI.typeValue, URI),
            Pair(INPOS.typeValue, INPOS),
            Pair(TDDATA.typeValue, TDDATA),
            Pair(LESFEAT.typeValue, LESFEAT),
            Pair(CMAPUP.typeValue, CMAPUP),
            Pair(PBADV.typeValue, PBADV),
            Pair(MMESSAGE.typeValue, MMESSAGE),
            Pair(MBEACON.typeValue, MBEACON),
            Pair(BIGINFO.typeValue, BIGINFO),
            Pair(BCCODE.typeValue, BCCODE),
            Pair(RSID.typeValue, RSID),
            Pair(ADINTL.typeValue, ADINTL),
            Pair(DATA3D.typeValue, DATA3D),
            Pair(MFR.typeValue, MFR)
        )

        fun getGAPType(typeValue: String): String {
            return if (gapMap.containsKey(typeValue)) {
                gapMap[typeValue]!!.typeName
            } else {
                ""
            }
        }
    }
}