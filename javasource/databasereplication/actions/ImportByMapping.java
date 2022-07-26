// This file was generated by Mendix Studio Pro.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package databasereplication.actions;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.core.objectmanagement.member.MendixDateTime;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixIdentifier;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.IMendixObjectMember;
import com.mendix.systemwideinterfaces.core.UserAction;
import com.mendix.webui.CustomJavaAction;
import databasereplication.implementation.DBReplicationSettings;
import databasereplication.implementation.DBReplicationSettings.JoinType;
import databasereplication.implementation.DBReplicationSettings.JoinedTable;
import databasereplication.implementation.DatabaseConnector;
import databasereplication.implementation.IDataManager;
import databasereplication.implementation.ObjectBaseDBSettings;
import databasereplication.implementation.DbTypes.IDatabaseConnector;
import databasereplication.interfaces.IDatabaseSettings;
import databasereplication.proxies.AdditionalJoins;
import databasereplication.proxies.AssociationKeyOption;
import databasereplication.proxies.Column;
import databasereplication.proxies.ColumnMapping;
import databasereplication.proxies.Constraint;
import databasereplication.proxies.CustomConstraint;
import databasereplication.proxies.Database;
import databasereplication.proxies.ImportCall;
import databasereplication.proxies.MappingType;
import databasereplication.proxies.Mode;
import databasereplication.proxies.Operator;
import databasereplication.proxies.QueryType;
import databasereplication.proxies.ReferenceDataHandling;
import databasereplication.proxies.ReferenceHandling;
import databasereplication.proxies.ReferenceHandlingEnum;
import databasereplication.proxies.RemoveIndicator;
import databasereplication.proxies.Table;
import databasereplication.proxies.TableMapping;
import databasereplication.proxies.YesNo;
import mxmodelreflection.proxies.Microflows;
import mxmodelreflection.proxies.MxObjectMember;
import mxmodelreflection.proxies.MxObjectReference;
import mxmodelreflection.proxies.MxObjectType;
import replication.AssociationConfig;
import replication.ObjectConfig;
import replication.ReplicationSettings.AssociationDataHandling;
import replication.ReplicationSettings.ChangeTracking;
import replication.ReplicationSettings.KeyType;
import replication.ReplicationSettings.MendixReplicationException;
import replication.ReplicationSettings.ObjectSearchAction;
import replication.ValueParser;
import replication.implementation.MFValueParser;
import replication.interfaces.IInfoHandler.StatisticsLevel;
import replication.interfaces.IValueParser;

/**
 * This action triggers the import for the tablemapping
 * 
 * The parameter ApplyEntityAccess specifies if the import should run withor without all the security constraints of the user who triggers the import.
 * When you change this parameter to false, it will run without any security limitations (i.e. as MxAdministrator)
 * 
 * 
 * The result of this action is always 'true', if the import fails at any point it will return in an exception. 
 * 
 * The rollback behaviour is determined by your microflow and the transaction parameters in the TableMapping.
 */
public class ImportByMapping extends CustomJavaAction<java.lang.Boolean>
{
	private IMendixObject __Mapping;
	private databasereplication.proxies.TableMapping Mapping;
	private java.lang.Boolean ApplyEntityAccess;

	public ImportByMapping(IContext context, IMendixObject Mapping, java.lang.Boolean ApplyEntityAccess)
	{
		super(context);
		this.__Mapping = Mapping;
		this.ApplyEntityAccess = ApplyEntityAccess;
	}

	@java.lang.Override
	public java.lang.Boolean executeAction() throws Exception
	{
		this.Mapping = this.__Mapping == null ? null : databasereplication.proxies.TableMapping.initialize(getContext(), __Mapping);

		// BEGIN USER CODE
		IContext context = this.getContext();

		Database CurDatabase = this.Mapping.getTableMapping_Database(context);

		ObjectBaseDBSettings dbSettings = new ObjectBaseDBSettings( context, CurDatabase.getMendixObject() );
		importFromDatabase(context, this.Mapping, dbSettings, this, this.ApplyEntityAccess);
		return true;
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 * @return a string representation of this action
	 */
	@java.lang.Override
	public java.lang.String toString()
	{
		return "ImportByMapping";
	}

	// BEGIN EXTRA CODE
	public static IMendixObject importFromDatabase(IContext context, TableMapping Mapping, IDatabaseSettings dbSettings, UserAction<?> action, Boolean applyEntityAcces ) throws MendixReplicationException, CoreException
	{
		return InitialiseImport(context, Mapping, dbSettings).startSynchronizing(action, applyEntityAcces);
	}

	public static IMendixObject importFromDatabaseWithImportCall(IContext context, TableMapping Mapping, IDatabaseSettings dbSettings, UserAction<?> action, ImportCall importCall, IMendixObject objectOne, IMendixObject objectTwo, Boolean applyEntityAcces )
	throws MendixReplicationException, CoreException
	{
		if(importCall.getImportCallOne_MxObjectType() != null)
		{
			if(!Core.isSubClassOf(importCall.getImportCallOne_MxObjectType(context).getCompleteName(), objectOne.getType()))
			{
				throw new CoreException("The input object one is not of type: "+importCall.getImportCallOne_MxObjectType(context).getCompleteName()+" but is "+objectOne.getType());
			}
		}
		if(importCall.getImportCalTwo_MxObjectType() != null)
		{
			if(!Core.isSubClassOf(importCall.getImportCalTwo_MxObjectType(context).getCompleteName(), objectTwo.getType()))
			{
				throw new CoreException("The input object two is not of type: "+importCall.getImportCalTwo_MxObjectType(context).getCompleteName()+" but is "+objectTwo.getType());
			}
		}

		IDataManager manager = InitialiseImport( context, Mapping, dbSettings );
		DBReplicationSettings settings = manager.getSettings();
		
		if( importCall.getSetReferenceToEntityOne( context ) ) {
			MxObjectReference objectref = importCall.getImportCallOne_MxObjectReference( context );
			if( objectref == null )
				throw new CoreException("An association should be set to import object one, but this object isn't provided.");
			
			settings.setParentAssociation( objectref.getCompleteName( context ) );
			settings.setParentObjectId( objectOne );
		} else if( importCall.getSetReferenceToEntityTwo( context ) ) {
			MxObjectReference objectref = importCall.getImportCallTwo_MxObjectReference( context );
			if( objectref == null )
				throw new CoreException("An association should be set to import object two, but this object isn't provided.");
			
			settings.setParentAssociation( objectref.getCompleteName( context ) );
			settings.setParentObjectId( objectTwo );
		}
		
		List<IMendixObject> constraints = Core.retrieveXPathQuery(context, "//"+CustomConstraint.getType()+"["+CustomConstraint.MemberNames.CustomConstraint_ImportCall.toString()+"="+importCall.getMendixObject().getId().toLong()+"]");


		String constraint = importCall.getConstraintDependency(context);
		if( constraint == null )
			constraint = "";

		String escapeClose = dbSettings.getDatabaseConnection().getEscapeClose();
		String escapeOpen = dbSettings.getDatabaseConnection().getEscapeOpen();

		if( importCall.getConstraintMode() == Mode.Advanced ) {
			if( constraints.size() != 0 && "".equals(constraint) )
				throw new CoreException("Unable to prepare the import configuration, there is a custom constraint specified but without any constraints. Error occurred while processing mapping: " + Mapping.getDescription(context) );
		}
		else
			constraint = "";

		if( Core.getLogger("DBReplication").isTraceEnabled() )
			Core.getLogger("DBReplication").trace("Star processing query: " + settings.getQuery());

		for(int i = 0; i < constraints.size(); i++)
		{
			CustomConstraint custom = CustomConstraint.initialize(context, constraints.get(i));

			AdditionalJoins join = custom.getCustomConstraint_AdditionalJoins();
			Column column = custom.getCustomConstraint_Column();

			String where = null;
			switch(custom.getConstraintType())
			{
			case Static_Constraint:
				if(custom.getValue() != null) {
					String alias = join.getIsFromTable() ? settings.getFromTableAlias() : join.getAlias();
					where = escapeOpen+formatAlias(alias)+escapeClose+"."+
					escapeOpen+column.getName()+escapeClose+" "+parseOperator(custom.getOperator())+" "+custom.getValue();
				}
				else if( !custom.getIgnoreWhenEmpty() ) {
					String alias = join.getIsFromTable() ? settings.getFromTableAlias() : join.getAlias();
					where = escapeOpen+formatAlias(alias)+escapeClose+"."+
					escapeOpen+column.getName()+escapeClose+" "+parseOperator(custom.getOperator())+" NULL ";
				}
				else if( importCall.getConstraintMode() == Mode.Advanced )
					throw new MendixReplicationException("Unable to create the import query for Mapping: " + Mapping.getDescription(context) + ". Because constraint: " + custom.getNr() + " has the property 'ignore when empty' set to true. This isn't allowed when constraint mode is set to 'Advanced'");
				break;
			case Dynamic_Constraint:
				String value = getObjectValue(context, objectOne, objectTwo, custom);
				if(value != null)
				{
					String alias = join.getIsFromTable() ? settings.getFromTableAlias() : join.getAlias();
					where = escapeOpen+formatAlias(alias)+escapeClose+"."+
					escapeOpen+column.getName()+escapeClose+" "+parseOperator(custom.getOperator())+" '"+value+"'";
				}
				else if( !custom.getIgnoreWhenEmpty() ) {
					String alias = join.getIsFromTable() ? settings.getFromTableAlias() : join.getAlias();
					where = escapeOpen+formatAlias(alias)+escapeClose+"."+
					escapeOpen+column.getName()+escapeClose+" "+parseOperator(custom.getOperator())+" NULL ";
				}
				else if( importCall.getConstraintMode() == Mode.Advanced )
					throw new MendixReplicationException("Unable to create the import query for Mapping: " + Mapping.getDescription(context) + ". Because constraint: " + custom.getNr() + " has the property 'ignore when empty' set to true. This isn't allowed when constraint mode is set to 'Advanced'");
				break;
			}

			if(where != null)
			{
				Core.getLogger("DBReplication").debug("Replacing: " + String.valueOf(custom.getNr(context)) + " with '" + where + "' in " + constraint );
				if( importCall.getConstraintMode() == Mode.Advanced )
					constraint = constraint.replace( String.valueOf(custom.getToken(context)), where);
				else {
					if(constraint == null || "".equals(constraint) )
						constraint = where;
					else
						constraint += " AND " +where;
				}
				Core.getLogger("DBReplication").trace("Constraint value after replacement: " + constraint);
			}
		}

		settings.addConstraint(constraint);

		Core.getLogger("DBReplication").info("SQL query: " + settings.getQuery());

		return manager.startSynchronizing(action, applyEntityAcces);
	}

	public static IDataManager InitialiseImport(IContext context, TableMapping Mapping, IDatabaseSettings dbSettings ) throws MendixReplicationException, CoreException {
		DBReplicationSettings settings = new DBReplicationSettings(context, dbSettings, Mapping.getTableMapping_MxObjectType(context).getCompleteName(), null);

		Table table = Mapping.getTableMapping_Table(context);
		if( table == null )
			throw new MendixReplicationException("Invalid table configuration the mapping should reference a table");
		
		String fromAlias = formatAlias(settings.setFromTable( table.getName() ));
		String xpathQuery = "//" + AdditionalJoins.getType() + "[" + AdditionalJoins.MemberNames.AdditionalJoins_TableMapping + "=" + Mapping.getMendixObject().getId().toLong() + "][not(" + AdditionalJoins.MemberNames.AdditionalJoins_AdditionalJoins_Left + "/" + AdditionalJoins.getType() + ")]";
		List<IMendixObject> joinTables = Core.retrieveXPathQuery(context, xpathQuery);
		if( joinTables.size() == 1 ) {
			ArrayList<String> tableList = new ArrayList<String>();
			tableList.add(fromAlias);
			processJoinedTable( context, Mapping, settings, joinTables.get(0), 1, tableList );
		}
		else if( joinTables.size() > 1 )
			throw new MendixReplicationException("Invalid joined tables, there are 1 or more joined tables that haven't defined a left table.");

		ObjectConfig mainObjectConfig = settings.getMainObjectConfig();

		/*
		 * Process all columns and copy their values in the ReplicationSetting class
		 */
		List<IMendixObject> colMapping = Core.retrieveXPathQuery(context, "//" + ColumnMapping.getType() + "[" + ColumnMapping.MemberNames.ColumnMapping_TableMapping + "=" + Mapping.getMendixObject().getId().toLong() + "]");
		for( IMendixObject object : colMapping ) {
			IMendixIdentifier memberId = (IMendixIdentifier)object.getValue(context, ColumnMapping.MemberNames.ColumnMapping_MxObjectMember.toString());
			if( memberId == null )
					throw new MendixReplicationException("Invalid Configuration, No Member selected for column: " + object.getValue(context, "Info_MxMapping") + " - " + object.getValue(context, "Info_DbMapping") );
					
			IMendixObject member = Core.retrieveId(context, memberId );

			//Determine the Key type
			AssociationKeyOption isKeyOption = AssociationKeyOption.valueOf( (String)object.getValue(context, ColumnMapping.MemberNames.IsAssociationKey.toString()));
			KeyType isKey = null;
			switch (isKeyOption) {
			case No:
				isKey = KeyType.NoKey;
				break;
			case YesOnlyMainObject:
				isKey = KeyType.ObjectKey;
				break;
			case YesOnlyAssociatedObject:
				isKey = KeyType.AssociationKey;
				break;
			case YesMainAndAssociatedObject:
				isKey = KeyType.AssociationAndObjectKey;
				break;
			}

			//Find a possible value parsing action
			IValueParser parser = null;
			if( YesNo.Yes == YesNo.valueOf((String)object.getValue(context, ColumnMapping.MemberNames.UseFormatMicroflow.toString()) ) ) {
				IMendixIdentifier mfId = (IMendixIdentifier)object.getValue(context, ColumnMapping.MemberNames.ColumnMapping_Microflows.toString());
				if( mfId == null )
					throw new MendixReplicationException("Invalid Configuration, No Microflow selected for column: " + object.getValue(context, "Info_MxMapping") + " - " + object.getValue(context, "Info_DbMapping") );
				parser = new MFValueParser(context, Core.retrieveId(context, mfId ));
			}

			// Configure the Query and the member mapping
			if( QueryType.Automatic.toString().equals(object.getValue(context, ColumnMapping.MemberNames.QueryType.toString())) ) {
				IMendixIdentifier joinId = (IMendixIdentifier) object.getValue(context, ColumnMapping.MemberNames.ColumnMapping_AdditionalJoins.toString());
				IMendixIdentifier columnId = (IMendixIdentifier)object.getValue(context, ColumnMapping.MemberNames.ColumnMapping_Column.toString());
				if( joinId == null )
					throw new MendixReplicationException("Invalid Configuration, No Table selected for column: " + object.getValue(context, "Info_MxMapping") + " - " + object.getValue(context, "Info_DbMapping") );
				if( columnId == null )
					throw new MendixReplicationException("Invalid Configuration, No Column selected for column: " + object.getValue(context, "Info_MxMapping") + " - " + object.getValue(context, "Info_DbMapping") );


				IMendixObject joinedTable = Core.retrieveId(context, joinId);
				IMendixObject column = Core.retrieveId(context, columnId);

				String tableAlias = formatAlias((String) joinedTable.getValue(context, AdditionalJoins.MemberNames.Alias.toString()));
				if( (Boolean) joinedTable.getValue(context, AdditionalJoins.MemberNames.IsFromTable.toString() ) )
					tableAlias = fromAlias;

				if( MappingType.Attribute.toString().equals(object.getValue(context, ColumnMapping.MemberNames.MappingType.toString())) ) {
					settings.addColumnMapping(tableAlias, (String)column.getValue(context, Column.MemberNames.Name.toString()),
							(String)member.getValue(context, MxObjectMember.MemberNames.AttributeName.toString()), isKey,
							(Boolean)object.getValue(context, ColumnMapping.MemberNames.SearchCaseSensitive.toString()), parser );
				}
				else {
					IMendixIdentifier refId = (IMendixIdentifier)object.getValue(context, ColumnMapping.MemberNames.ColumnMapping_MxObjectReference.toString()),
							objectId = (IMendixIdentifier)object.getValue(context, ColumnMapping.MemberNames.ColumnMapping_MxObjectType.toString());
					
					if( refId == null )
						throw new MendixReplicationException("Invalid Configuration, No Association selected for column: " + object.getValue(context, "Info_MxMapping") + " - " + object.getValue(context, "Info_DbMapping") );
					if( objectId == null )
						throw new MendixReplicationException("Invalid Configuration, No Object Type selected for column: " + object.getValue(context, "Info_MxMapping") + " - " + object.getValue(context, "Info_DbMapping") );
					
					IMendixObject reference = Core.retrieveId(context, refId);
					IMendixObject objectType = Core.retrieveId(context, objectId);

					settings.addAssociationMapping(tableAlias, (String)column.getValue(context, Column.MemberNames.Name.toString()),
							(String)reference.getValue(context, MxObjectReference.MemberNames.CompleteName.toString()),
							(String)objectType.getValue(context, MxObjectType.MemberNames.CompleteName.toString()),
							(String)member.getValue(context, MxObjectMember.MemberNames.AttributeName.toString()), parser,
							isKey, (Boolean)object.getValue(context, ColumnMapping.MemberNames.SearchCaseSensitive.toString()) );
				}
			}

			// Copy the Query from the column and configure member mapping
			else {
				if( MappingType.Attribute.toString().equals(object.getValue(context, ColumnMapping.MemberNames.MappingType.toString())) ) {
					settings.addCustomColumnMapping( processStatement(context, settings.getDbSettings().getDatabaseConnection(), Mapping, (String)object.getValue(context, ColumnMapping.MemberNames.SelectStatement.toString()), fromAlias),
							(String)member.getValue(context, MxObjectMember.MemberNames.AttributeName.toString()), isKey,
							(Boolean)object.getValue(context, ColumnMapping.MemberNames.SearchCaseSensitive.toString()), parser );
				}
				else {
					IMendixObject reference = Core.retrieveId(context, (IMendixIdentifier)object.getValue(context, ColumnMapping.MemberNames.ColumnMapping_MxObjectReference.toString()));
					IMendixObject objectType = Core.retrieveId(context, (IMendixIdentifier)object.getValue(context, ColumnMapping.MemberNames.ColumnMapping_MxObjectType.toString()));

					settings.addCustomAssociationMapping( processStatement(context, settings.getDbSettings().getDatabaseConnection(), Mapping, (String)object.getValue(context, ColumnMapping.MemberNames.SelectStatement.toString()), fromAlias),
							(String)reference.getValue(context, MxObjectReference.MemberNames.CompleteName.toString()),
							(String)objectType.getValue(context, MxObjectType.MemberNames.CompleteName.toString()),
							(String)member.getValue(context, MxObjectMember.MemberNames.AttributeName.toString()), parser,
							isKey, (Boolean)object.getValue(context, ColumnMapping.MemberNames.SearchCaseSensitive.toString()) );
				}
			}
		}

		/*
		 * Update all the settings for the configured references
		 */
		List<IMendixObject> refHandlingList = Core.retrieveXPathQuery(context, "//" + ReferenceHandling.getType() + "[" + ReferenceHandling.MemberNames.ReferenceHandling_TableMapping + "=" + Mapping.getMendixObject().getId().toLong() + "]");
		for(IMendixObject object : refHandlingList ) {
			if( object.getValue(context, ReferenceHandling.MemberNames.ReferenceHandling_MxObjectReference.toString()) == null )
				Core.delete(context, object);
			else {
				IMendixObject refObj = Core.retrieveId(context, (IMendixIdentifier) object.getValue(context, ReferenceHandling.MemberNames.ReferenceHandling_MxObjectReference.toString()));
				String associationName = (String)refObj.getValue(context, MxObjectReference.MemberNames.CompleteName.toString());

				AssociationConfig config = settings.getAssociationConfig(associationName);
				
				ReferenceHandlingEnum refHandling = ReferenceHandlingEnum.valueOf((String)object.getValue(context, ReferenceHandling.MemberNames.Handling.toString()));
				switch (refHandling) {
				case FindCreate:
					config.setObjectSearchAction(ObjectSearchAction.FindCreate);
					break;
				case FindIgnore:
					config.setObjectSearchAction(ObjectSearchAction.FindIgnore);
					break;
				case CreateEverything:
					config.setObjectSearchAction(ObjectSearchAction.CreateEverything);
					break;
				case OnlyCreateNewObjects:
					config.setObjectSearchAction(ObjectSearchAction.OnlyCreateNewObjects);
					break;
				}

				ReferenceDataHandling refDataHandling = ReferenceDataHandling.valueOf((String)object.getValue(context, ReferenceHandling.MemberNames.DataHandling.toString()));
				switch( refDataHandling ) {
				case Append:
					config.setAssociationDataHandling(AssociationDataHandling.Append);
					break;
				case Overwrite:
					config.setAssociationDataHandling(AssociationDataHandling.Overwrite);
					break;
				}

				config.setCommitUnchangedObjects( (Boolean) object.getValue(context, ReferenceHandling.MemberNames.CommitUnchangedObjects.toString()) );
				config.setPrintNotFoundMessages( (Boolean) object.getValue(context, ReferenceHandling.MemberNames.PrintNotFoundMessages.toString()) );
				config.setIgnoreEmptyKeys( (Boolean) object.getValue(context, ReferenceHandling.MemberNames.IgnoreEmptyKeys.toString()) );
			}
		}

		
		Microflows flow = Mapping.getTableMapping_Microflows(context);
		if( flow != null ) {
			settings.addFinishingMicroflow( flow.getCompleteName() );
		}
		
		RemoveIndicator indicator = Mapping.getRemoveUnsyncedObjects(context);
		if( indicator == null ) {
			indicator = RemoveIndicator.Nothing;
		}

		ObjectConfig mainConfig = settings.getMainObjectConfig();
		switch ( indicator ) {
		case RemoveUnchangedObjects :
			MxObjectMember member = Mapping.getTableMapping_MxObjectMember(context);
			mainConfig.removeUnusedObjects( ChangeTracking.RemoveUnchangedObjects,  member.getAttributeName(context) );
			break;
		case TrackChanges :
			MxObjectMember membertracking = Mapping.getTableMapping_MxObjectMember(context);
			mainConfig.removeUnusedObjects( ChangeTracking.TrackChanges,  membertracking.getAttributeName(context) );
			break;
		case Nothing :
		default :
			mainConfig.removeUnusedObjects( ChangeTracking.Nothing, null );
			break;
		}

		settings.setConstraint( Mapping.getSQLConstraint() );

		switch ( Mapping.getPrintStatisticsMessages() ) {
		case AllStatistics : 
			settings.printImportStatistics( StatisticsLevel.AllStatistics );
			break;
		case OnlyFinalStatistics : 
			settings.printImportStatistics( StatisticsLevel.OnlyFinalStatistics );
			break;
		case NoStatistics :
			settings.printImportStatistics( StatisticsLevel.NoStatistics );
			break;
		default: 
			settings.printImportStatistics( StatisticsLevel.AllStatistics );
			break;
		}

		if( Mode.Advanced.equals(Mapping.getMode()) ) {
			settings.importInNewContext(Mapping.getImportInNewContext());
			settings.useTransactions(Mapping.getUseTransactions());
			settings.ignoreEmptyKeys(Mapping.getIgnoreEmptyKeys());
			settings.resetEmptyAssociations(Mapping.getResetEmptyAssociations());
			settings.getMainObjectConfig().setCommitUnchangedObjects(Mapping.getCommitUnchangedObjects_MainObject());
			settings.getMainObjectConfig().setPrintNotFoundMessages(Mapping.getPrintNotFoundMessages_MainObject());
		}

		switch (Mapping.getImportAction()) {
		case Synchronize:
			mainObjectConfig.setObjectSearchAction( ObjectSearchAction.FindCreate );
			break;
		case SynchronizeOnlyExisting:
			mainObjectConfig.setObjectSearchAction( ObjectSearchAction.FindIgnore );
			break;
		case Create:
			mainObjectConfig.setObjectSearchAction( ObjectSearchAction.CreateEverything );
			break;
		case OnlyCreateNewObjects:
			mainObjectConfig.setObjectSearchAction( ObjectSearchAction.OnlyCreateNewObjects );
			break;
		}
		
//		settings.Configuration.RetrieveById_Limit = 20; //default 200
//		settings.Configuration.RetrieveOQL_Limit = 10; //default 1000
//		settings.Configuration.RetrieveToBeRemovedObjectsXPath_Limit = 200; //default 200
//		settings.Configuration.MetaInfoProcessingBatchThreshold = 10; //default 1000
		
		return IDataManager.instantiate( Mapping.getMendixObject(), Mapping.getDescription(), settings );
	}

	private static void processJoinedTable(IContext context, TableMapping Mapping, DBReplicationSettings settings, IMendixObject additionalJoinObject, int joinLevel, ArrayList<String> processedTables) throws CoreException {

		JoinType joinType = JoinType.valueOf( (String) additionalJoinObject.getValue(context, AdditionalJoins.MemberNames.JoinType.toString()));
		String tableNameRight = formatAlias((String)(Core.retrieveId(context, (IMendixIdentifier)additionalJoinObject.getValue(context, AdditionalJoins.MemberNames.AdditionalJoins_Table_Right.toString()) )).getValue(context, Table.MemberNames.Name.toString()));
		IMendixIdentifier leftId = (IMendixIdentifier)additionalJoinObject.getValue(context, AdditionalJoins.MemberNames.AdditionalJoins_AdditionalJoins_Left.toString());
		
		if( leftId != null ) {
			IMendixObject leftJoin = Core.retrieveId(context, leftId );
			String tableNameLeft = formatAlias( settings.getAliasForJoinedTable( context, leftJoin ) );
			String alias = additionalJoinObject.getValue(context, AdditionalJoins.MemberNames.Alias.toString());
			processedTables.add(alias);
			
			if( joinType == JoinType.FROM ) { 
				settings.joinTable(joinType, tableNameRight, formatAlias(alias));
			}
			else { 
				List<IMendixObject> constraints = Core.retrieveXPathQuery(context, "//" + Constraint.getType() + "[" + Constraint.MemberNames.Constraint_AdditionalJoins + "=" + additionalJoinObject.getId().toLong() + "]");
				
				/*
				 * First we need to validate the joined tables and make sure we join them in  the correct order. 
				 */
				for( IMendixObject object : constraints ) {
					String constraintLeftTable = tableNameLeft;
					IMendixIdentifier leftTableId = (IMendixIdentifier)object.getValue(context, Constraint.MemberNames.Constraint_AdditionalJoins_Left.toString());
					
					if( leftTableId != null ) {
						IMendixObject leftTable = Core.retrieveId(context, leftTableId );
						constraintLeftTable = formatAlias( settings.getAliasForJoinedTable( context, leftTable ) );
						if( !settings.hasTableAlias(constraintLeftTable) && !processedTables.contains(constraintLeftTable)) {
							processJoinedTable(context, Mapping, settings, leftTable, joinLevel, processedTables);
						}
					}
					else if( !settings.hasTableAlias(constraintLeftTable) && !processedTables.contains(constraintLeftTable) ) {
						IMendixObject leftTable = Core.retrieveId(context, ((IMendixIdentifier)leftJoin.getValue(context, AdditionalJoins.MemberNames.AdditionalJoins_Table_Right.toString())));
						processJoinedTable(context, Mapping, settings, leftTable, joinLevel, processedTables);
					}
				}
				
				/*
				 * Now create the joined table and append the constraints
				 */
				JoinedTable table = settings.joinTable(joinType, tableNameRight, formatAlias(alias));
				for( IMendixObject object : constraints ) {
					IMendixIdentifier leftColId = (IMendixIdentifier) object.getValue(context, Constraint.MemberNames.Constraint_Column_Left.toString());
					IMendixIdentifier rightColId = (IMendixIdentifier) object.getValue(context, Constraint.MemberNames.Constraint_Column_Right.toString());

					if( leftColId == null )
						throw new CoreException("The left column is not selected for joined table: " + tableNameRight + " - " + alias + " in mapping " + Mapping.getDescription() );
					if( rightColId == null )
						throw new CoreException("The right column is not selected for joined table: " + tableNameRight + " - " + alias + " in mapping " + Mapping.getDescription() );
					
					String constraintLeftTable = tableNameLeft;
					IMendixIdentifier leftTableId = (IMendixIdentifier)object.getValue(context, Constraint.MemberNames.Constraint_AdditionalJoins_Left.toString());
					if( leftTableId != null ) {
						IMendixObject leftTable = Core.retrieveId(context, leftTableId );
						constraintLeftTable = formatAlias( settings.getAliasForJoinedTable( context, leftTable ) );
					}
					
					String leftColumnName = (Core.retrieveId(context, leftColId )).getValue(context, Column.MemberNames.Name.toString());
					String rightColumnName  = (Core.retrieveId(context, rightColId )).getValue(context, Column.MemberNames.Name.toString());;
					table.addConstraint(rightColumnName, constraintLeftTable, leftColumnName);			
				}
			}
		}

		List<IMendixObject> joinTables = Core.retrieveXPathQuery(context, "//" + AdditionalJoins.getType() + "[" + AdditionalJoins.MemberNames.AdditionalJoins_TableMapping + "=" + Mapping.getMendixObject().getId().toLong() + "][" + AdditionalJoins.MemberNames.AdditionalJoins_AdditionalJoins_Left + "=" + additionalJoinObject.getId().toLong() + "]");
		for( IMendixObject object : joinTables ) {
			String alias = formatAlias( settings.getAliasForJoinedTable( context, object ) );
			if( !processedTables.contains(alias) )
				processJoinedTable(context, Mapping, settings, object, joinLevel+1, processedTables);
		}
	}

	public static String formatAlias( String alias ) {
		alias = alias.replace(" ", "");

		return alias;
	}

	private static String processStatement( IContext context, IDatabaseConnector dbType, TableMapping Mapping, String statement, String fromAlias ) throws CoreException {
		List<IMendixObject> joins = Core.retrieveXPathQuery(context, "//" + AdditionalJoins.getType()+ "[" + AdditionalJoins.MemberNames.AdditionalJoins_TableMapping + "=" + Mapping.getMendixObject().getId().toLong() + "]");
		for( IMendixObject join : joins ) {
			String alias = formatAlias((String)join.getValue(context, AdditionalJoins.MemberNames.Alias.toString()));
			if( (Boolean) join.getValue(context,  AdditionalJoins.MemberNames.IsFromTable.toString() ) )
				alias = fromAlias;
			
			statement = statement.replace((String)join.getValue(context, AdditionalJoins.MemberNames.Token.toString()), DatabaseConnector.processTableAlias(dbType, alias));
		}
		return statement;
	}

	private static String parseOperator(Operator operator)
	{
		switch(operator)
		{
		case Equal:
			return "=";
		case Larger:
			return ">";
		case LargerOrEqual:
			return ">=";
		case Smaller:
			return "<";
		case SmallerOrEqual:
			return "<=";
		case NotEqual:
			return "!=";
		case Is_not_NULL:
			return "IS NOT";
		case Is_NULL:
			return "IS";
		default:
			return "=";
		}
	}

	private static String getObjectValue(IContext context, IMendixObject objectOne, IMendixObject objectTwo, CustomConstraint custom) throws CoreException
	{
		MxObjectMember member = custom.getCustomConstraint_MxObjectMember();
		switch(custom.getInputObject())
		{
                    case Object1:
                            return parseValueToString(objectOne.getMember(context, member.getAttributeName()), context);
                    case Object2:
                            return parseValueToString(objectTwo.getMember(context, member.getAttributeName()), context);
		}

		return "";
	}

	private static String parseValueToString(Object data, IContext context) throws ValueParser.ParseException
	{
		if (data == null)
		{
			return null;
		}
		else if (data instanceof Integer)
		{
			return Integer.toString(ValueParser.getIntegerValue(data));
		}
		else if (data instanceof Boolean)
		{
			return Boolean.toString(ValueParser.getBooleanValue(data));
		}
		else if (data instanceof Double)
		{
			return Double.toString(ValueParser.getDoubleValue(data));
		}
		else if (data instanceof Float)
		{
			return Float.toString((Float) data);
		}
		else if (data instanceof Date)
		{
            return formatDate((Date) data);
		}
		else if (data instanceof Long)
		{
			return Long.toString(ValueParser.getLongValue(data));
		}
		else if (data instanceof IMendixObjectMember<?>)
		{
			IMendixObjectMember<?> member = (IMendixObjectMember<?>) data;
			if (member.getValue(context) == null)
			{
				return null;
			}
			
			if (data instanceof MendixDateTime)
			{
				MendixDateTime datetime = (MendixDateTime) data;
				return formatDate(datetime.getValue(context));
			}
			else
			{
				return ((IMendixObjectMember<?>) data).parseValueToString(context);
			}
		}
		if (data instanceof String)
		{
			return ValueParser.getTrimmedValue(data, null, null);
		}
		return null;
	}

	private static String formatDate(Date date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date.toInstant().atZone(ZoneId.systemDefault()));
    }

	// END EXTRA CODE
}
