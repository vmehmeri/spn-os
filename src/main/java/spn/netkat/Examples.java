package spn.netkat;

public class Examples {
    public static Policy generate2node2linkNetKATProgram() {
        //NetKAT routing policy program using local compilation for demo_2node2link Topology 20150130-1000
        /*
          (((filter (switch = 1)) ;
          (((filter ((ipSrc = 10.0.0.1) and (ipDst = 10.0.0.2))) ; (port := 1)) |
          ((filter (ipDst = 10.0.0.1)) ; (port := 2)))) |
          ((filter (switch = 2)) ;
          (((filter ((ipSrc = 10.0.0.2) and (ipDst = 10.0.0.1))) ; (port := 1)) |
          ((filter (ipDst = 10.0.0.2)) ; (port := 2)))))
        */
        //NetKAT routing policy program written in Java for demo_2node2link Topology
        Policy twoNodeTwoLinkSw1Policy =
            new Sequence(
                new Filter(new Test("switch", "1")),
                new Union(
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.1"),
                                           new Test("ipDst", "10.0.0.2"))),
                        new Modification("port", "1")
                        ),
                    new Sequence(
                        new Filter(new Test("ipDst", "10.0.0.1")),
                        new Modification("port", "2")
                        )
                    )
                );
        Policy twoNodeTwoLinkSw2Policy =
            new Sequence(
                new Filter(new Test("switch", "2")),
                new Union(
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.2"),
                                           new Test("ipDst", "10.0.0.1"))),
                        new Modification("port", "1")
                        ),
                    new Sequence(
                        new Filter(new Test("ipDst", "10.0.0.2")),
                        new Modification("port", "2")
                        )
                    )
                );
        Policy twoNodeTwoLinkNetPolicy = new Union(twoNodeTwoLinkSw1Policy, twoNodeTwoLinkSw2Policy);
        System.out.println("Main.generate2node2linkNetKakProgram: NetKAT Program for 2node2link Network sent to NetKAT Compiler: " + twoNodeTwoLinkNetPolicy);
        //End of NetKAT routing policy program written in Java for demo_2node2link Topology
        return twoNodeTwoLinkNetPolicy;
    }

    public static Policy generateVNO123NetKATProgram() {
        //Native NetKAT Program for VNO1 routing policy using local compilation 20150203-1430
        /*
          (((filter (switch = 1)) ;
          (((filter ((ipSrc = 10.0.0.1) and (ipDst = 10.0.0.2))) ; (port := 2)) |
          ((filter ((ipSrc = 10.0.0.1) and (ipDst = 10.0.0.3))) ; (port := 1)) |
          ((filter ((ipSrc = 10.0.0.2) and (ipDst = 10.0.0.1))) ; (port := 3)) |
          ((filter ((ipSrc = 10.0.0.3) and (ipDst = 10.0.0.1))) ; (port := 3)) |
          ((filter ((ipSrc = 10.0.0.3) and (ipDst = 10.0.0.2))) ; (port := 2)))) |

          ((filter (switch = 2)) ;
          (((filter ((ipSrc = 10.0.0.2) and (ipDst = 10.0.0.1))) ; (port := 2)) |
          ((filter ((ipSrc = 10.0.0.2) and (ipDst = 10.0.0.3))) ; (port := 2)) |
          ((filter ((ipSrc = 10.0.0.1) and (ipDst = 10.0.0.2))) ; (port := 5)) |
          ((filter ((ipSrc = 10.0.0.3) and (ipDst = 10.0.0.2))) ; (port := 5)) |
          ((filter ((ipSrc = 10.0.0.3) and (ipDst = 10.0.0.1))) ; (port := 1)))) |

          ((filter (switch = 4)) ;
          (((filter ((ipSrc = 10.0.0.3) and (ipDst = 10.0.0.1))) ; (port := 1)) |
          ((filter ((ipSrc = 10.0.0.3) and (ipDst = 10.0.0.2))) ; (port := 3)) |
          ((filter ((ipSrc = 10.0.0.1) and (ipDst = 10.0.0.3))) ; (port := 5)) |
          ((filter ((ipSrc = 10.0.0.2) and (ipDst = 10.0.0.3))) ; (port := 5)) |
          ((filter ((ipSrc = 10.0.0.2) and (ipDst = 10.0.0.1))) ; (port := 3)))) |

          ((filter (switch = 5)) ;
          (((filter ((ipSrc = 10.0.0.2) and (ipDst = 10.0.0.1))) ; (port := 1)) |
          ((filter ((ipSrc = 10.0.0.1) and (ipDst = 10.0.0.3))) ; (port := 2)) |
          ((filter ((ipSrc = 10.0.0.3) and (ipDst = 10.0.0.2))) ; (port := 1)))))
        */
        //Java-based NetKAT Program for VNO1 20150203-1430
        Policy VNO1SW1Policy =
            new Sequence(
                new Filter(new Test("switch", "1")),
                new Union(
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.1"),
                                           new Test("ipDst", "10.0.0.2"))),
                        new Modification("port", "2")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.1"),
                                           new Test("ipDst", "10.0.0.3"))),
                        new Modification("port", "1")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.2"),
                                           new Test("ipDst", "10.0.0.1"))),
                        new Modification("port", "3")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.3"),
                                           new Test("ipDst", "10.0.0.1"))),
                        new Modification("port", "3")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.3"),
                                           new Test("ipDst", "10.0.0.2"))),
                        new Modification("port", "2")
                        )
                    )
                );

        Policy VNO1SW2Policy =
            new Sequence(
                new Filter(new Test("switch", "2")),
                new Union(
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.2"),
                                           new Test("ipDst", "10.0.0.1"))),
                        new Modification("port", "2")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.2"),
                                           new Test("ipDst", "10.0.0.3"))),
                        new Modification("port", "2")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.1"),
                                           new Test("ipDst", "10.0.0.2"))),
                        new Modification("port", "5")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.3"),
                                           new Test("ipDst", "10.0.0.2"))),
                        new Modification("port", "5")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.3"),
                                           new Test("ipDst", "10.0.0.1"))),
                        new Modification("port", "1")
                        )
                    )
                );

        Policy VNO1SW4Policy =
            new Sequence(
                new Filter(new Test("switch", "4")),
                new Union(
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.3"),
                                           new Test("ipDst", "10.0.0.1"))),
                        new Modification("port", "1")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.3"),
                                           new Test("ipDst", "10.0.0.2"))),
                        new Modification("port", "3")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.1"),
                                           new Test("ipDst", "10.0.0.3"))),
                        new Modification("port", "5")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.2"),
                                           new Test("ipDst", "10.0.0.3"))),
                        new Modification("port", "5")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.2"),
                                           new Test("ipDst", "10.0.0.1"))),
                        new Modification("port", "3")
                        )
                    )
                );

        Policy VNO1SW5Policy =
            new Sequence(
                new Filter(new Test("switch", "5")),
                new Union(
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.2"),
                                           new Test("ipDst", "10.0.0.1"))),
                        new Modification("port", "1")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.1"),
                                           new Test("ipDst", "10.0.0.3"))),
                        new Modification("port", "2")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.3"),
                                           new Test("ipDst", "10.0.0.2"))),
                        new Modification("port", "1")
                        )
                    )
                );

        Policy VNO1Policy = new Union(VNO1SW1Policy, VNO1SW2Policy, VNO1SW4Policy, VNO1SW5Policy);
        System.out.println("Main.generateVNO123NetKATProgram: VNO1Policy: " + VNO1Policy);
        //End of Java-based NetKAT Program for VNO1


        //Native NetKAT Program for VNO2 routing policy using local compilation 20150203-1440
        /*
          (((filter (switch = 4)) ;
          (((filter ((ipSrc = 10.0.0.4) and (ipDst = 10.0.0.5))) ; (port := 4)) |
          ((filter ((ipSrc = 10.0.0.5) and (ipDst = 10.0.0.4))) ; (port := 6)))) |

          ((filter (switch = 2)) ;
          (((filter ((ipSrc = 10.0.0.5) and (ipDst = 10.0.0.4))) ; (port := 4)) |
          ((filter ((ipSrc = 10.0.0.4) and (ipDst = 10.0.0.5))) ; (port := 6)))) |

          ((filter (switch = 7)) ;
          (((filter ((ipSrc = 10.0.0.4) and (ipDst = 10.0.0.5))) ; (port := 1)) |
          ((filter ((ipSrc = 10.0.0.5) and (ipDst = 10.0.0.4))) ; (port := 2)))))
        */
        //Java-based NetKAT Program for VNO2 20150203-1440
        Policy VNO2SW4Policy =
            new Sequence(
                new Filter(new Test("switch", "4")),
                new Union(
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.4"),
                                           new Test("ipDst", "10.0.0.5"))),
                        new Modification("port", "4")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.5"),
                                           new Test("ipDst", "10.0.0.4"))),
                        new Modification("port", "6")
                        )
                    )
                );

        Policy VNO2SW2Policy =
            new Sequence(
                new Filter(new Test("switch", "2")),
                new Union(
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.5"),
                                           new Test("ipDst", "10.0.0.4"))),
                        new Modification("port", "4")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.4"),
                                           new Test("ipDst", "10.0.0.5"))),
                        new Modification("port", "6")
                        )
                    )
                );

        Policy VNO2SW7Policy =
            new Sequence(
                new Filter(new Test("switch", "7")),
                new Union(
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.4"),
                                           new Test("ipDst", "10.0.0.5"))),
                        new Modification("port", "1")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.5"),
                                           new Test("ipDst", "10.0.0.4"))),
                        new Modification("port", "2")
                        )
                    )
                );

        Policy VNO2Policy = new Union(VNO2SW4Policy, VNO2SW2Policy, VNO2SW7Policy);
        System.out.println("Main.generateVNO123NetKATProgram: VNO2Policy: " + VNO2Policy);
        //End of Java-based NetKAT Program for VNO2


        //Native NetKAT Program for VNO3 routing policy using local compilation 20150203-1450
        /*
          (((filter (switch = 1)) ;
          (((filter ((ipSrc = 10.0.0.1) and (ipDst = 10.0.0.6))) ; (port := 1)) |
          ((filter ((ipSrc = 10.0.0.6) and (ipDst = 10.0.0.1))) ; (port := 3)) |
          ((filter ((ipSrc = 10.0.0.2) and (ipDst = 10.0.0.6))) ; (port := 1)))) |

          ((filter (switch = 2)) ;
          (((filter ((ipSrc = 10.0.0.6) and (ipDst = 10.0.0.1))) ; (port := 1)) |
          ((filter ((ipSrc = 10.0.0.2) and (ipDst = 10.0.0.6))) ; (port := 1)) |
          ((filter ((ipSrc = 10.0.0.6) and (ipDst = 10.0.0.2))) ; (port := 5)))) |

          ((filter (switch = 3)) ;
          (((filter ((ipSrc = 10.0.0.1) and (ipDst = 10.0.0.6))) ; (port := 4)) |
          ((filter ((ipSrc = 10.0.0.2) and (ipDst = 10.0.0.6))) ; (port := 4)) |
          ((filter ((ipSrc = 10.0.0.6) and (ipDst = 10.0.0.1))) ; (port := 3)) |
          ((filter ((ipSrc = 10.0.0.6) and (ipDst = 10.0.0.2))) ; (port := 2)))) |

          ((filter (switch = 4)) ;
          (((filter ((ipSrc = 10.0.0.1) and (ipDst = 10.0.0.6))) ; (port := 2)) |
          ((filter ((ipSrc = 10.0.0.6) and (ipDst = 10.0.0.1))) ; (port := 1)) |
          ((filter ((ipSrc = 10.0.0.2) and (ipDst = 10.0.0.6))) ; (port := 4)) |
          ((filter ((ipSrc = 10.0.0.6) and (ipDst = 10.0.0.2))) ; (port := 1)))) |

          ((filter (switch = 5)) ;
          (((filter ((ipSrc = 10.0.0.1) and (ipDst = 10.0.0.6))) ; (port := 2)) |
          ((filter ((ipSrc = 10.0.0.2) and (ipDst = 10.0.0.6))) ; (port := 2)))) |

          ((filter (switch = 6)) ;
          (((filter ((ipSrc = 10.0.0.6) and (ipDst = 10.0.0.1))) ; (port := 2)) |
          ((filter ((ipSrc = 10.0.0.2) and (ipDst = 10.0.0.6))) ; (port := 1)))) |

          ((filter (switch = 7)) ;
          (((filter ((ipSrc = 10.0.0.6) and (ipDst = 10.0.0.1))) ; (port := 2)) |
          ((filter ((ipSrc = 10.0.0.2) and (ipDst = 10.0.0.6))) ; (port := 3)))))
        */
        //Java-based NetKAT Program for VNO3 20150203-1450
        Policy VNO3SW1Policy =
            new Sequence(
                new Filter(new Test("switch", "1")),
                new Union(
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.1"),
                                           new Test("ipDst", "10.0.0.6"))),
                        new Modification("port", "1")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.6"),
                                           new Test("ipDst", "10.0.0.1"))),
                        new Modification("port", "3")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.2"),
                                           new Test("ipDst", "10.0.0.6"))),
                        new Modification("port", "1")
                        )
                    )
                );

        Policy VNO3SW2Policy =
            new Sequence(
                new Filter(new Test("switch", "2")),
                new Union(
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.6"),
                                           new Test("ipDst", "10.0.0.1"))),
                        new Modification("port", "1")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.2"),
                                           new Test("ipDst", "10.0.0.6"))),
                        new Modification("port", "1")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.6"),
                                           new Test("ipDst", "10.0.0.2"))),
                        new Modification("port", "5")
                        )
                    )
                );

        Policy VNO3SW3Policy =
            new Sequence(
                new Filter(new Test("switch", "3")),
                new Union(
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.1"),
                                           new Test("ipDst", "10.0.0.6"))),
                        new Modification("port", "4")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.2"),
                                           new Test("ipDst", "10.0.0.6"))),
                        new Modification("port", "4")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.6"),
                                           new Test("ipDst", "10.0.0.1"))),
                        new Modification("port", "3")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.6"),
                                           new Test("ipDst", "10.0.0.2"))),
                        new Modification("port", "2")
                        )
                    )
                );

        Policy VNO3SW4Policy =
            new Sequence(
                new Filter(new Test("switch", "4")),
                new Union(
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.1"),
                                           new Test("ipDst", "10.0.0.6"))),
                        new Modification("port", "2")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.6"),
                                           new Test("ipDst", "10.0.0.1"))),
                        new Modification("port", "1")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.2"),
                                           new Test("ipDst", "10.0.0.6"))),
                        new Modification("port", "4")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.6"),
                                           new Test("ipDst", "10.0.0.2"))),
                        new Modification("port", "1")
                        )
                    )
                );

        Policy VNO3SW5Policy =
            new Sequence(
                new Filter(new Test("switch", "5")),
                new Union(
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.1"),
                                           new Test("ipDst", "10.0.0.6"))),
                        new Modification("port", "2")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.2"),
                                           new Test("ipDst", "10.0.0.6"))),
                        new Modification("port", "2")
                        )
                    )
                );

        Policy VNO3SW6Policy =
            new Sequence(
                new Filter(new Test("switch", "6")),
                new Union(
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.6"),
                                           new Test("ipDst", "10.0.0.1"))),
                        new Modification("port", "2")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.2"),
                                           new Test("ipDst", "10.0.0.6"))),
                        new Modification("port", "1")
                        )
                    )
                );

        Policy VNO3SW7Policy =
            new Sequence(
                new Filter(new Test("switch", "7")),
                new Union(
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.6"),
                                           new Test("ipDst", "10.0.0.1"))),
                        new Modification("port", "2")
                        ),
                    new Sequence(
                        new Filter(new And(new Test("ipSrc", "10.0.0.2"),
                                           new Test("ipDst", "10.0.0.6"))),
                        new Modification("port", "3")
                        )
                    )
                );

        Policy VNO3Policy = new Union(VNO3SW1Policy, VNO3SW2Policy, VNO3SW3Policy, VNO3SW4Policy, VNO3SW5Policy, VNO3SW6Policy, VNO3SW7Policy);
        System.out.println("Main.generateVNO123NetKATProgram: VNO3Policy: " + VNO3Policy);
        //End of Java-based NetKAT Program for VNO3

        //Generate combined policy for VNO1, VNO2 and VNO3;
        Policy GlobalPolicy = new Union(VNO1Policy, VNO2Policy, VNO3Policy);
        System.out.println("Main.generateVNO123NetKATProgram: Global NetKAT Program for VNO123 Network sent to NetKAT Compiler: " + GlobalPolicy);

        return GlobalPolicy;
    }

    public static Policy generateVNO1NetKATProgram() {
        Policy VNO1Policy =
            new Union(
                new Sequence(
                    new Filter(new Test("ipDst", "10.0.0.1")),
                    new Modification("port", "1")
                    ),
                new Sequence(
                    new Filter(new Test("ipDst", "10.0.0.2")),
                    new Modification("port", "2")
                    ),
                new Sequence(
                    new Filter(new Test("ipDst", "10.0.0.3")),
                    new Modification("port", "3")
                    )
                );
        System.out.println("Main.generateVNO1NetKATProgram: VNO1Policy: " + VNO1Policy);
        return VNO1Policy;
    }

    public static Policy generateVNOxNetKATProgram() {
        Policy VSW1Policy =
            new Sequence(
                new Filter(new Test("switch", "1")),
                new Union(
                    new Sequence(
                        new Filter(new Test("ipDst", "10.0.0.1")),
                        new Modification("port", "1")
                        ),
                    new Sequence(
                        new Filter(new Test("ipDst", "10.0.0.4")),
                        new Modification("port", "2")
                        ),
                    new Sequence(
                        new Filter(new Test("ipDst", "10.0.0.6")),
                        new Modification("port", "2")
                        ),
                    new Sequence(
                        new Filter(new Test("ipDst", "10.0.0.5")),
                        new Modification("port", "3")
                        )
                    )
                );

        Policy VSW2Policy =
            new Sequence(
                new Filter(new Test("switch", "2")),
                new Union(
                    new Sequence(
                        new Filter(new Test("ipDst", "10.0.0.1")),
                        new Modification("port", "2")
                        ),
                    new Sequence(
                        new Filter(new Test("ipDst", "10.0.0.4")),
                        new Modification("port", "2")
                        ),
                    new Sequence(
                        new Filter(new Test("ipDst", "10.0.0.6")),
                        new Modification("port", "3")
                        ),
                    new Sequence(
                        new Filter(new Test("ipDst", "10.0.0.5")),
                        new Modification("port", "1")
                        )
                    )
                );

        Policy VSW3Policy =
            new Sequence(
                new Filter(new Test("switch", "3")),
                new Union(
                    new Sequence(
                        new Filter(new Test("ipDst", "10.0.0.1")),
                        new Modification("port", "2")
                        ),
                    new Sequence(
                        new Filter(new Test("ipDst", "10.0.0.4")),
                        new Modification("port", "2")
                        ),
                    new Sequence(
                        new Filter(new Test("ipDst", "10.0.0.6")),
                        new Modification("port", "1")
                        ),
                    new Sequence(
                        new Filter(new Test("ipDst", "10.0.0.5")),
                        new Modification("port", "3")
                        )
                    )
                );

        Policy VSW4Policy =
            new Sequence(
                new Filter(new Test("switch", "4")),
                new Union(
                    new Sequence(
                        new Filter(new Test("ipDst", "10.0.0.1")),
                        new Modification("port", "2")
                        ),
                    new Sequence(
                        new Filter(new Test("ipDst", "10.0.0.4")),
                        new Modification("port", "1")
                        ),
                    new Sequence(
                        new Filter(new Test("ipDst", "10.0.0.6")),
                        new Modification("port", "3")
                        ),
                    new Sequence(
                        new Filter(new Test("ipDst", "10.0.0.5")),
                        new Modification("port", "2")
                        )
                    )
                );

        Policy VNOxPolicy = new Union(VSW1Policy, VSW2Policy, VSW3Policy, VSW4Policy);
        System.out.println("Main.generateVNOxNetKATProgram: VNOxPolicy: " + VNOxPolicy);
        return VNOxPolicy;
    }

    // POlicies on a single switch with two hosts connected

    public static Policy generateSingle() {
        Policy oneToTwo = new Sequence(
            new Filter(new Test("switch", "1")),
            new Union(
                new Sequence(
                    new Filter(new Test("ethdst", "00:00:00:00:04:01")),
                    new Modification("port", "2")
                    ),
                new Sequence(
                    new Filter(new Test("ethdst", "00:00:00:00:03:01")),
                    new Modification("port", "1")
                    )
                )
            );
        Policy twoToOne = new Sequence(
            new Filter(new Test("switch", "2")),
            new Union(
                new Sequence(
                    new Filter(new Test("ethdst", "00:00:00:00:03:01")),
                    new Modification("port", "2")
                    ),
                new Sequence(
                    new Filter(new Test("ethdst", "00:00:00:00:04:01")),
                    new Modification("port", "1")
                    )
                )
            );
        Policy singlePolicy = new Union(oneToTwo, twoToOne);
        System.out.println("generateSingle: singlePolicy: " + singlePolicy);
        return singlePolicy;
    }

    public static Policy generateSingleIngress() {
        Policy atPortOne = new Sequence(
            new Filter(
                new And(
                    new Test("port", "1"),
                    new Test("switch", "1"))),
            new Modification("vport", "1"),
            new Modification("vswitch", "1"));
        Policy atPortTwo = new Sequence(
            new Filter(
                new And(
                    new Test("port", "1"),
                    new Test("switch", "2"))),
            new Modification("vport", "1"),
            new Modification("vswitch", "2"));
        Policy singleIngress =
            new Union(atPortOne, atPortTwo);
        System.out.println("generateSingleIngress : singleIngress : " + singleIngress);
        return singleIngress;
    }

    public static Policy generateSOSR1() {
        return generateVNO1NetKATProgram();
    }

    public static Policy generateSOSRIngress1() {
        Policy vswitch = new Modification("vswitch", "1");
        Policy vport = new IfThenElse(
                new And(new Test("switch", "1"), new Test("port", "3")),
                new Modification("vport", "1"),
                new IfThenElse(
                    new And(new Test("switch", "2"), new Test("port", "5")),
                    new Modification("vport", "2"),
                    new IfThenElse(
                        new And(new Test("switch", "4"), new Test("port", "5")),
                        new Modification("vport", "3"),
                        new Drop())));
        Policy SOSRIngress1 = new Sequence(vswitch, vport);
        System.out.println("generateSOSRIngress1 : SOSRIngress : " + SOSRIngress1);
        return SOSRIngress1;
    }

    public static String getSOSRIngress1() {
        return " vswitch:=1;\n" +
            "if switch=1 and port=3 then vport:=1 \n" +
            "else if switch=2 and port=5 then vport:=2\n" +
            "else if switch=4 and port=5 then vport:=3\n" +
            "else drop";
    }

    public static Policy generateSOSR2() {
        return generateVNOxNetKATProgram();
    }

    public static Policy generateSOSRIngress2() {
        Policy vport = new Modification("vport", "1");
        Policy vswitch = new IfThenElse(
            new And(new Test("switch", "1"), new Test("port", "3")),
            new Modification("vswitch", "1"),
            new IfThenElse(
                new And(new Test("switch", "2"), new Test("port", "6")),
                new Modification("vswitch", "2"),
                new IfThenElse(
                    new And(new Test("switch", "3"), new Test("port", "4")),
                    new Modification("vswitch", "3"),
                    new IfThenElse(
                        new And(new Test("switch", "4"), new Test("port", "6")),
                        new Modification("vswitch", "4"),
                        new Drop()))));

        Policy SOSRIngress2 = new Sequence(vport, vswitch);
        System.out.println("generateSOSRIngress1 : SOSRIngress : " + SOSRIngress2.toString());
        return SOSRIngress2;
    }

    public static String getSOSRIngress2() {
        return "vport:=1;\n" +
            "if switch=1 and port=3 then vswitch:=1\n" +
            "else if switch=2 and port=6 then vswitch:=2\n" +
            "else if switch=3 and port=4 then vswitch:=3\n" +
            "else if switch=4 and port=6 then vswitch:=4\n" +
            "else drop";
    }
}
