# Python script creating a spectrum analysis

# Initializing a new empty session
MySession=IANewSession()

# Material creation
MySession.LoadMaterial('materialName','2024_T351_Plate','AIMS03-02-004','Referenced')

# Spectrum analysis creation
MySession.CreateStandaloneAnalysis2('analysisName','spectrum_analysis','',
[
   # Step parameters
   ['/CsmMbr_MapProcessParameterSet/CsmMbr_ProcessParameterMap[CaesamStd_StepProcessParameter]/Execute', 'BA:TRUE:4::TRUE;TRUE;TRUE;FALSE'], # Check Validity; Initiation; Propagation; Residual Strength
   
   # Sequence Loading
   ['EO[SequenceLoading]/LoadingSequenceType','Enum_LoadingSequenceType:COMPLEX STRESS LOADING'], # Loading sequence type
   ['EO[SequenceLoading]/EO_COMPLEX_STRESS_LOADING','AirbusEO_DSigmaComplexStressLoading:'], # Loading sequence type contd.
   ['EO[SequenceLoading]/EO_COMPLEX_STRESS_LOADING/SpectrumUrl/Url','Caesam_Url:file:/projects/a350_ima_ia_training/PRIVATE_DATA/Equinox/Isami_Tests/Fuselage_F26_MY.sigma'], # Sigma file path
   ['EO[SequenceLoading]/EO_COMPLEX_STRESS_LOADING/AlreadyRainflowed','Enum_AlreadyRainflowed:NOT RAINFLOWED'], # Rainflow status for input spectrum
   ['EO[SequenceLoading]/EO_COMPLEX_STRESS_LOADING/PeakMultiplicativeCoefficient','CaesamQty_DIMENSIONLESS:1;none'], # Multiplicative coefficient
   ['EO[SequenceLoading]/EO_COMPLEX_STRESS_LOADING/PeakStandingStress','D:0'], # Shift stress
   
   # Analysis Law
   ['EO[FatigueLaw]/StructureMaterial','AirbusEO_TMaterial:materialName'], # Structure material
   ['EO[FatigueLaw]/LawType','Enum_ToggleLawType:Fatigue Law'], # Law type
   ['EO[FatigueLaw]/DamageCalculationMethod','Enum_ToggleDamageCalculationMethodGeoIndependent:AFI USER DEFINED'], # Damage calculation method
   ['EO[FatigueLaw]/Orientation_init','Enum_Orientation:LT'], # Damage law orientation (LS/LT/SL/TL/TS)
   ['EO[FatigueLaw]/Configuration_init','S:Configuration:AFI/thickness:6-12'], # Damage law configuration or failure mode
   ['EO[FatigueLaw]/UserDefinedAFI','CaesamQty_PRESSURE:100;MPa'], # User defined AFI
   ['EO[FatigueLaw]/FatigueLaw','Enum_ToggleFatigueLaw:AFI LAW'], #
]
,True)

# Run analyses
MySession.RunAllAnalysis()  

# Saving session
MySession.Save('/projects/a350_ima_ia_training/PRIVATE_DATA/Equinox/Isami_Tests/Fuselage_F26_MY.czm')
	
# Close session
MySession.Close()
