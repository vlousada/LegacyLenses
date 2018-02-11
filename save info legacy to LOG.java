//SAVE LEGACY INFO to LOG.TXT
        String imgFileDate = "";
        String legacyLensName = "";
        String legacyLensAperture = "";
        String legacyLensSpecial = "";
        //GET LAST IMAGE Filename and DateTime
        imgFileDate = LLUtils.getLastImage(this);
        legacyLensName = m_tvLegacyLensName.getText().toString();
        legacyLensAperture = m_tvLegacyAperture.getText().toString();
        legacyLensSpecial = m_tvLegacySpecial.getText().toString();
        Logger.exif(imgFileDate+";"+legacyLensName+";"+legacyLensAperture+";"+legacyLensSpecial);