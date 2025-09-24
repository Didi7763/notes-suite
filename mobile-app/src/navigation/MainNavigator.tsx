import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createStackNavigator } from '@react-navigation/stack';
import Icon from 'react-native-vector-icons/MaterialIcons';
import { MainTabParamList, RootStackParamList } from '../types';
import NotesScreen from '../screens/NotesScreen';
import PublicNotesScreen from '../screens/PublicNotesScreen';
import ProfileScreen from '../screens/ProfileScreen';
import NoteEditorScreen from '../screens/NoteEditorScreen';
import NoteViewerScreen from '../screens/NoteViewerScreen';

const Tab = createBottomTabNavigator<MainTabParamList>();
const Stack = createStackNavigator<RootStackParamList>();

const TabNavigator: React.FC = () => {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        tabBarIcon: ({ focused, color, size }) => {
          let iconName: string;

          if (route.name === 'Notes') {
            iconName = 'note';
          } else if (route.name === 'Public') {
            iconName = 'public';
          } else if (route.name === 'Profile') {
            iconName = 'person';
          } else {
            iconName = 'help';
          }

          return <Icon name={iconName} size={size} color={color} />;
        },
        tabBarActiveTintColor: '#1976d2',
        tabBarInactiveTintColor: 'gray',
        headerShown: false,
      })}
    >
      <Tab.Screen 
        name="Notes" 
        component={NotesScreen}
        options={{ title: 'Mes Notes' }}
      />
      <Tab.Screen 
        name="Public" 
        component={PublicNotesScreen}
        options={{ title: 'Publiques' }}
      />
      <Tab.Screen 
        name="Profile" 
        component={ProfileScreen}
        options={{ title: 'Profil' }}
      />
    </Tab.Navigator>
  );
};

const MainNavigator: React.FC = () => {
  return (
    <Stack.Navigator>
      <Stack.Screen 
        name="Main" 
        component={TabNavigator}
        options={{ headerShown: false }}
      />
      <Stack.Screen 
        name="NoteEditor" 
        component={NoteEditorScreen}
        options={{ title: 'Éditeur de Note' }}
      />
      <Stack.Screen 
        name="NoteViewer" 
        component={NoteViewerScreen}
        options={{ title: 'Voir la Note' }}
      />
    </Stack.Navigator>
  );
};

export default MainNavigator;
