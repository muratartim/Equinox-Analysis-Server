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
   ['EO[SequenceLoading]/EO_COMPLEX_STRESS_LOADING/SpectrumUrl/Url','Caesam_Url:file:/projects/a350_ima_ia_training/PRIVATE_DATA/Equinox/Isami_Tests/Fuselage_F26_MY.sigma'], # File Type
   ['EO[SequenceLoading]/EO_COMPLEX_STRESS_LOADING/AlreadyRainflowed','Enum_AlreadyRainflowed:NOT RAINFLOWED'], # Rainflow status for input spectrum
   ['EO[SequenceLoading]/EO_COMPLEX_STRESS_LOADING/PeakMultiplicativeCoefficient','CaesamQty_DIMENSIONLESS:1;none'], # Multiplicative coefficient
   ['EO[SequenceLoading]/EO_COMPLEX_STRESS_LOADING/PeakStandingStress','D:0'], # Shift stress
   
   # Analysis Law
   ['EO[FatigueLaw]/StructureMaterial','AirbusEO_TMaterial:materialName'], # Structural material
   ['EO[FatigueLaw]/LawType','Enum_ToggleLawType:Propagation Law'], # Law type
   ['EO[FatigueLaw]/PropagationLaw','Enum_PropagationLaw:Elber'], # Propagation law
   ['EO[FatigueLaw]/Orientation_propa','Enum_Orientation:LT'], # Propagation law orientation
   ['EO[FatigueLaw]/Configuration_propa','S:Configuration:ELBER (AF)/thickness:0-7.5'], # Propagation law configuration
   ['EO[FatigueLaw]/RetardationModel','Enum_RetardationModel:Preffas'], # Retardation model
   ['EO[FatigueLaw]/ConsideredCompression','CaesamEnum_YesNo:Yes'], # Considered compression
   ['EO[FatigueLaw]/PropagationOmission','CaesamEnum_YesNo:No'], # Omission for crack propagation
]
,True)

# Run analyses
MySession.RunAllAnalysis()  

# Saving session
MySession.Save('/projects/a350_ima_ia_training/PRIVATE_DATA/Equinox/Isami_Tests/Fuselage_F26_MY.czm')
	
# Close session
MySession.Close()
