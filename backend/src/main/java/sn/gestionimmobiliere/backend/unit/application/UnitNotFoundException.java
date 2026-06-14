package sn.gestionimmobiliere.backend.unit.application;
import java.util.UUID;
public class UnitNotFoundException extends RuntimeException { public UnitNotFoundException(UUID id){super("Unite introuvable: "+id);} }
